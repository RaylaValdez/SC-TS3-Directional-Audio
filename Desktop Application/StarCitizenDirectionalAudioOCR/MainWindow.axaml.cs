using Avalonia;
using Avalonia.Controls;
using Avalonia.Controls.Primitives;   // for RangeBase & ToggleButton
using Avalonia.Threading;
using System;
using System.Diagnostics;
using System.IO;
using System.Threading;
using System.Threading.Tasks;

namespace StarCitizenDirectionalAudioOCR;

public partial class MainWindow : Avalonia.Controls.Window
{
    private readonly RoiStore _roi = new();
    private readonly CaptureService _cap = new();
    private readonly OcrService _ocr = new();
    private CancellationTokenSource? _cts;

    // Output targets (bind whatever your XAML actually uses)
    private TextBlock? _parsedA;   // ParsedText
    private TextBlock? _parsedB;   // LastParsed
    private TextBlock? _parsedC;   // LastParsedText
    private TextBlock? _parsedD;   // LastParsedBlock

    // Thread-safe mirror of TickSlider.Value so the STA loop never touches the UI
    private volatile int _tickRate = 10;

    // Only show last valid parsed output
    private string _lastValidParsed = "";
    // Staleness tracking (last time we got a valid parse)
    private DateTime _lastValidAt = DateTime.MinValue;

    public MainWindow()
    {
        InitializeComponent();

        StartBtn.Click += async (_, __) => await StartAsync();
        StopBtn.Click += (_, __) => Stop();
        CalibrateBtn.Click += async (_, __) => await CalibrateAsync();

        // "Stay on top" toggle
        var topBtn = this.FindControl<ToggleButton>("TopmostToggle");
        if (topBtn != null)
        {
            topBtn.IsChecked = this.Topmost; // reflect current state
            topBtn.Checked += (_, __) => this.Topmost = true;
            topBtn.Unchecked += (_, __) => this.Topmost = false;
        }

        // Grab output labels and wire up slider on the UI thread
        Dispatcher.UIThread.Post(() =>
        {
            _parsedA = this.FindControl<TextBlock>("ParsedText");
            _parsedB = this.FindControl<TextBlock>("LastParsed");
            _parsedC = this.FindControl<TextBlock>("LastParsedText");
            _parsedD = this.FindControl<TextBlock>("LastParsedBlock");

            // Seed and mirror tick rate (RangeBase.Value is double)
            _tickRate = (int)Math.Clamp(TickSlider.Value, 1, 60);
            TickSlider.PropertyChanged += (_, e) =>
            {
                if (e.Property == RangeBase.ValueProperty)
                    _tickRate = (int)Math.Clamp(TickSlider.Value, 1, 60);
            };
        });
    }

    private async Task StartAsync()
    {
        if (_cts != null) return;

        StartBtn.IsEnabled = false;
        StopBtn.IsEnabled = true;
        StatusText.Text = "Locating Star Citizen…";

        // Clear last valid on start (keeps UI blank until first valid)
        _lastValidParsed = "";
        _lastValidAt = DateTime.UtcNow;   // start the staleness clock
        SetOutput(""); // parsed-only setter (will keep blank until valid)

        _cts = new();
        Log.Info("Start OCR clicked.");

        // Locate once with timeout
        var locateTask = _cap.WaitForStarCitizenRectAsync();
        var finished = await Task.WhenAny(locateTask, Task.Delay(TimeSpan.FromSeconds(10), _cts.Token));
        if (finished != locateTask)
        {
            StatusText.Text = "Timeout locating Star Citizen.";
            Log.Info("Locate timed out.");
            Stop();
            return;
        }
        var winRect = await locateTask;
        if (winRect == null)
        {
            await MessageBox("Could not find Star Citizen window.");
            Log.Info("Locate returned null.");
            Stop();
            return;
        }

        StatusText.Text = $"Found SC @ {winRect.Value.Left},{winRect.Value.Top}  {winRect.Value.Width}x{winRect.Value.Height}";
        Log.Info($"Found SC window: L={winRect.Value.Left} T={winRect.Value.Top} W={winRect.Value.Width} H={winRect.Value.Height}");

        // Compute absolute ROI
        var roiFractions = _roi.LoadOrDefault();
        var absRoi = _cap.ComputeAbsoluteRoi(winRect.Value, roiFractions);
        Log.Info($"ABS ROI: X={absRoi.X} Y={absRoi.Y} W={absRoi.Width} H={absRoi.Height}");

        // One-shot probe on UI thread (kept)
        try
        {
            using var testFrame = _cap.CaptureScreenRect(absRoi);
            OpenCvSharp.Cv2.ImWrite(Log.DataPath("roi_raw.png"), testFrame);
            using var eng = _ocr.CreateEngine(out var info);
            Log.Info("Engine OK (one-shot). " + info);

            int h = testFrame.Rows, mid = Math.Max(1, h / 2);
            var top = new OpenCvSharp.Mat(testFrame, new OpenCvSharp.Rect(0, 0, testFrame.Cols, mid));
            var bot = new OpenCvSharp.Mat(testFrame, new OpenCvSharp.Rect(0, mid, testFrame.Cols, h - mid));

            // For one-shot show both lines too (parsed-only)
            string parsedBoth = TryParseBoth(eng, top, bot, out var rawTop, out var rawBot);
            SetOutput(parsedBoth);

            Log.Info($"One-shot TOP raw: '{TrimForLog(rawTop, 160)}'");
            Log.Info($"One-shot BOT raw: '{TrimForLog(rawBot, 160)}'");
            Log.Info($"One-shot parsed both: '{parsedBoth}'");
        }
        catch (Exception ex) { Log.Info("One-shot OCR failed: " + ex); }

        // ---- Background OCR loop on a dedicated STA thread ----
        Log.Info("Starting background OCR loop (STA)...");
        var staThread = new Thread(() =>
        {
            try
            {
                using var engine = _ocr.CreateEngine(out string engInfo);
                Log.Info("Tesseract engine created (loop). " + engInfo);

                var sw = Stopwatch.StartNew();
                long lastLog = 0;

                while (_cts != null && !_cts.IsCancellationRequested)
                {
                    int tps = Math.Clamp(_tickRate, 1, 60);
                    int delayMs = Math.Max(1, 1000 / tps);

                    using var frame = _cap.CaptureScreenRect(absRoi);
                    if (frame.Empty())
                    {
                        Dispatcher.UIThread.Post(() => StatusText.Text = "Region not visible.");
                        Thread.Sleep(200);
                        continue;
                    }

                    // Split lines
                    int hTick = frame.Rows, midTick = Math.Max(1, hTick / 2);
                    using var topTick = new OpenCvSharp.Mat(frame, new OpenCvSharp.Rect(0, 0, frame.Cols, midTick));
                    using var botTick = new OpenCvSharp.Mat(frame, new OpenCvSharp.Rect(0, midTick, frame.Cols, hTick - midTick));

                    // OCR and parse both lines independently; prefer parsed
                    string parsed = TryParseBoth(engine, topTick, botTick, out var rawTop, out var rawBot);

                    // UI update (parsed-only) + stale indicator
                    Dispatcher.UIThread.Post(() =>
                    {
                        var stale = (DateTime.UtcNow - _lastValidAt).TotalSeconds >= 2.0;
                        StatusText.Text = $"Running @ {tps} Hz{(stale ? " (stale)" : "")}";
                        // If we've been stale for 5s+, save a debug snapshot of both lines once.
                        if ((DateTime.UtcNow - _lastValidAt).TotalSeconds >= 5.0)
                        {
                            try
                            {
                                var p1 = Log.DataPath($"stale_top_{DateTime.Now:HHmmss}.png");
                                var p2 = Log.DataPath($"stale_bot_{DateTime.Now:HHmmss}.png");
                                OpenCvSharp.Cv2.ImWrite(p1, topTick);
                                OpenCvSharp.Cv2.ImWrite(p2, botTick);
                                // Prevent spamming; bump _lastValidAt so we don’t save again immediately
                                _lastValidAt = DateTime.UtcNow.AddSeconds(-3);
                                Log.Info($"Saved stale debug: {p1} / {p2}");
                            }
                            catch { }
                        }

                        SetOutput(parsed);
                    });

                    // Log ~1/sec (keep for debugging)
                    if (sw.ElapsedMilliseconds - lastLog >= 1000)
                    {
                        lastLog = sw.ElapsedMilliseconds;
                        Log.Info($"Tick TOP: '{TrimForLog(rawTop, 160)}'");
                        Log.Info($"Tick BOT: '{TrimForLog(rawBot, 160)}'");
                    }

                    Thread.Sleep(delayMs);
                }
            }
            catch (Exception ex)
            {
                Log.Info("OCR loop exception: " + ex);
                Dispatcher.UIThread.Post(() => StatusText.Text = "OCR stopped (error).");
            }
        });

        staThread.IsBackground = true;
        staThread.SetApartmentState(ApartmentState.STA);
        staThread.Start();
    }

    // OCR a line twice (bin on/off), return the better text and its parsed display
    // OCR both lines using multiple preprocess variants per line.
    // Returns combined parsed text (may be one or two lines).
    private string TryParseBoth(Tesseract.TesseractEngine engine, OpenCvSharp.Mat top, OpenCvSharp.Mat bot,
                                out string rawTopBest, out string rawBotBest)
    {
        var (rawT, parsedT) = OcrBestOfVariants(engine, top);
        var (rawB, parsedB) = OcrBestOfVariants(engine, bot);

        rawTopBest = rawT;
        rawBotBest = rawB;

        return CombineLines(parsedT, parsedB);
    }


    private static string CombineLines(string a, string b)
    {
        bool ea = string.IsNullOrWhiteSpace(a);
        bool eb = string.IsNullOrWhiteSpace(b);
        if (ea && eb) return "";
        if (ea) return b;
        if (eb) return a;
        return a + Environment.NewLine + b;
    }

    private void Stop()
    {
        _cts?.Cancel();
        _cts = null;
        StartBtn.IsEnabled = true;
        StopBtn.IsEnabled = false;
        StatusText.Text = "Stopped";
        Log.Info("Stop called.");
    }

    private async Task CalibrateAsync()
    {
        var rect = await _cap.WaitForStarCitizenRectAsync();
        if (rect == null) { await MessageBox("Could not find Star Citizen window."); return; }

        var (left, top, width, height) = rect.Value;
        var overlay = new RoiOverlayWindow(new Avalonia.Rect(left, top, width, height), _roi);
        await overlay.ShowDialog(this);
        Log.Info("Calibration dialog closed.");
    }

    private Task MessageBox(string msg) =>
        Dispatcher.UIThread.InvokeAsync(() =>
            new Window { Content = new TextBlock { Text = msg, Margin = new Thickness(16) }, Width = 320, Height = 120 }.ShowDialog(this));

    // --- helpers ---
    private void SetOutput(string parsedOnly)
    {
        // Only update when we have a valid parse; otherwise keep showing the previous good one.
        if (!string.IsNullOrWhiteSpace(parsedOnly))
        {
            _lastValidParsed = parsedOnly;
            _lastValidAt = DateTime.UtcNow;  // mark fresh

            _parsedA?.SetCurrentValue(TextBlock.TextProperty, _lastValidParsed);
            _parsedB?.SetCurrentValue(TextBlock.TextProperty, _lastValidParsed);
            _parsedC?.SetCurrentValue(TextBlock.TextProperty, _lastValidParsed);
            _parsedD?.SetCurrentValue(TextBlock.TextProperty, _lastValidParsed);
        }
    }

    private static string TrimForLog(string s, int n) => string.IsNullOrEmpty(s) ? "" : (s.Length <= n ? s : s.Substring(0, n).Replace("\n", " ") + "…");

    private static class Log
    {
        private static readonly string Dir =
            System.IO.Path.Combine(
                Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData),
                "SC-TS3-Directional-Audio");
        private static readonly string PathLog = System.IO.Path.Combine(Dir, "app.log");
        private static readonly object Gate = new();

        public static string DataPath(string fileName)
        {
            Directory.CreateDirectory(Dir);
            return System.IO.Path.Combine(Dir, fileName);
        }

        public static void Info(string msg)
        {
            try
            {
                Directory.CreateDirectory(Dir);
                var line = $"{DateTime.Now:HH:mm:ss.fff}  {msg}";
                lock (Gate) File.AppendAllText(PathLog, line + Environment.NewLine);
            }
            catch { /* ignore */ }
        }
    }

    // Try several preprocess variants for one line, pick the best by parse-count then length.
    private (string raw, string parsed) OcrBestOfVariants(Tesseract.TesseractEngine eng, OpenCvSharp.Mat line)
    {
        var bestRaw = "";
        var bestParsed = "";
        int bestMatches = -1;
        int bestLen = -1;

        var variants = _ocr.PreprocessVariants(line);
        foreach (var (tag, m) in variants)
        {
            using (m)
            {
                var txt = _ocr.Run(eng, m) ?? string.Empty;
                var items = Parser.ParseAll(txt);
                var parsed = Parser.FormatForDisplay(items);

                int matches = items.Count;
                int len = txt.Length;
                if (matches > bestMatches || (matches == bestMatches && len > bestLen))
                {
                    bestMatches = matches;
                    bestLen = len;
                    bestRaw = txt;
                    bestParsed = parsed;
                }
            }
        }
        return (bestRaw, bestParsed);
    }

}
