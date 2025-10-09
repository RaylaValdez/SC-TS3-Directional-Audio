using Avalonia;
using Avalonia.Controls;
using Avalonia.Controls.Primitives;
using Avalonia.Threading;
using System;
using System.Diagnostics;
using System.IO;
using System.Threading;
using System.Threading.Tasks;

namespace StarCitizenDirectionalAudioOCR;

public partial class MainWindow : Window
{
    private readonly RoiStore _roi = new();
    private readonly CaptureService _cap = new();
    private readonly OcrService _ocr = new();
    private CancellationTokenSource? _cts;

    // Output targets
    private TextBlock? _parsedA;
    private TextBlock? _parsedB;
    private TextBlock? _parsedC;
    private TextBlock? _parsedD;
    private TextBlock? _camDirLine;
    private TextBlock? _localPosLine;
    private TextBlock? _systemPosLine;

    // Tick rate (mirror slider)
    private volatile int _tickRate = 10;

    // Keep showing last VALID parsed text
    private string _lastValidParsed = "";
    private DateTime _lastValidAt = DateTime.MinValue;

    // NEW: cache last good telemetry + freshness
    private (double X, double Y, double Z)? _lastCam;
    private DateTime _lastCamAt = DateTime.MinValue;
    private ParsedPos? _lastLocal;
    private DateTime _lastLocalAt = DateTime.MinValue;
    private ParsedPos? _lastSystem;
    private DateTime _lastSystemAt = DateTime.MinValue;

    public MainWindow()
    {
        InitializeComponent();

        StartBtn.Click += async (_, __) => await StartAsync();
        StopBtn.Click += (_, __) => Stop();
        CalibrateBtn.Click += async (_, __) => await CalibrateAsync();

        var topBtn = this.FindControl<ToggleButton>("TopmostToggle");
        if (topBtn != null)
        {
            topBtn.IsChecked = this.Topmost;
            topBtn.Checked += (_, __) => this.Topmost = true;
            topBtn.Unchecked += (_, __) => this.Topmost = false;
        }

        Dispatcher.UIThread.Post(() =>
        {
            _parsedA = this.FindControl<TextBlock>("ParsedText");
            _parsedB = this.FindControl<TextBlock>("LastParsed");
            _parsedC = this.FindControl<TextBlock>("LastParsedText");
            _parsedD = this.FindControl<TextBlock>("LastParsedBlock");
            _camDirLine = this.FindControl<TextBlock>("CamDirLine");
            _localPosLine = this.FindControl<TextBlock>("LocalPosLine");
            _systemPosLine = this.FindControl<TextBlock>("SystemPosLine");

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

        _lastValidParsed = "";
        _lastValidAt = DateTime.UtcNow;
        SetOutput("");

        _lastCam = null;
        _lastLocal = null;
        _lastSystem = null;

        _cts = new();
        Log.Info("Start OCR clicked.");

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

        var roiFractions = _roi.LoadOrDefault();
        var absRoi = _cap.ComputeAbsoluteRoi(winRect.Value, roiFractions);
        Log.Info($"ABS ROI: X={absRoi.X} Y={absRoi.Y} W={absRoi.Width} H={absRoi.Height}");

        try
        {
            using var testFrame = _cap.CaptureScreenRect(absRoi);
            OpenCvSharp.Cv2.ImWrite(Log.DataPath("roi_raw.png"), testFrame);
            using var eng = _ocr.CreateEngine(out var info);
            Log.Info("Engine OK (one-shot). " + info);

            int h = testFrame.Rows;
            int h3 = Math.Max(1, h / 3);
            int h2 = Math.Max(1, h - 2 * h3);

            var top = new OpenCvSharp.Mat(testFrame, new OpenCvSharp.Rect(0, 0, testFrame.Cols, h3));
            var mid = new OpenCvSharp.Mat(testFrame, new OpenCvSharp.Rect(0, h3, testFrame.Cols, h2));
            var bot = new OpenCvSharp.Mat(testFrame, new OpenCvSharp.Rect(0, h3 + h2, testFrame.Cols, h - (h3 + h2)));

            string parsedThree = TryParseThree(eng, top, mid, bot,
                                               out var rawTop, out var rawMid, out var rawBot);
            SetOutput(parsedThree);

            Log.Info($"One-shot TOP raw: '{TrimForLog(rawTop, 160)}'");
            Log.Info($"One-shot MID raw: '{TrimForLog(rawMid, 160)}'");
            Log.Info($"One-shot BOT raw: '{TrimForLog(rawBot, 160)}'");
            Log.Info($"One-shot parsed: '{parsedThree}'");

            UpdateTelemetryFromRaw(rawTop, rawMid, rawBot);
        }
        catch (Exception ex) { Log.Info("One-shot OCR failed: " + ex); }

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

                    int hTick = frame.Rows;
                    int h3 = Math.Max(1, hTick / 3);
                    int h2 = Math.Max(1, hTick - 2 * h3);

                    using var topTick = new OpenCvSharp.Mat(frame, new OpenCvSharp.Rect(0, 0, frame.Cols, h3));
                    using var midTick = new OpenCvSharp.Mat(frame, new OpenCvSharp.Rect(0, h3, frame.Cols, h2));
                    using var botTick = new OpenCvSharp.Mat(frame, new OpenCvSharp.Rect(0, h3 + h2, frame.Cols, hTick - (h3 + h2)));

                    string parsed = TryParseThree(engine, topTick, midTick, botTick,
                                                  out var rawTop, out var rawMid, out var rawBot);

                    // Update telemetry from all three raw slices every tick
                    UpdateTelemetryFromRaw(rawTop, rawMid, rawBot);

                    Dispatcher.UIThread.Post(() =>
                    {
                        var stale = (DateTime.UtcNow - _lastValidAt).TotalSeconds >= 2.0;
                        StatusText.Text = $"Running @ {tps} Hz{(stale ? " (stale)" : "")}";
                        SetOutput(parsed);
                    });

                    if (sw.ElapsedMilliseconds - lastLog >= 1000)
                    {
                        lastLog = sw.ElapsedMilliseconds;
                        Log.Info($"Tick TOP: '{TrimForLog(rawTop, 160)}'");
                        Log.Info($"Tick MID: '{TrimForLog(rawMid, 160)}'");
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

    private static string CombineLines(string a, string b, string c)
    {
        bool ea = string.IsNullOrWhiteSpace(a);
        bool eb = string.IsNullOrWhiteSpace(b);
        bool ec = string.IsNullOrWhiteSpace(c);
        if (ea && eb && ec) return "";
        var parts = new System.Collections.Generic.List<string>(3);
        if (!ea) parts.Add(a);
        if (!eb) parts.Add(b);
        if (!ec) parts.Add(c);
        return string.Join(Environment.NewLine, parts);
    }

    private string TryParseThree(Tesseract.TesseractEngine engine,
                                 OpenCvSharp.Mat top,
                                 OpenCvSharp.Mat mid,
                                 OpenCvSharp.Mat bot,
                                 out string rawTopBest, out string rawMidBest, out string rawBotBest)
    {
        var (rawT, parsedT) = OcrBestOfVariants(engine, top);
        var (rawM, parsedM) = OcrBestOfVariants(engine, mid);
        var (rawB, parsedB) = OcrBestOfVariants(engine, bot);

        rawTopBest = rawT;
        rawMidBest = rawM;
        rawBotBest = rawB;

        return CombineLines(parsedT, parsedM, parsedB);
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

    private void SetOutput(string parsedOnly)
    {
        if (!string.IsNullOrWhiteSpace(parsedOnly))
        {
            _lastValidParsed = parsedOnly;
            _lastValidAt = DateTime.UtcNow;

            _parsedA?.SetCurrentValue(TextBlock.TextProperty, _lastValidParsed);
            _parsedB?.SetCurrentValue(TextBlock.TextProperty, _lastValidParsed);
            _parsedC?.SetCurrentValue(TextBlock.TextProperty, _lastValidParsed);
            _parsedD?.SetCurrentValue(TextBlock.TextProperty, _lastValidParsed);
        }
    }

    // -------- Telemetry update with caching & staleness (UI-thread safe) --------
    private void UpdateTelemetryFromRaw(string rawTop, string rawMid, string rawBot)
    {
        try
        {
            var combined = (rawTop ?? string.Empty) + "\n" +
                           (rawMid ?? string.Empty) + "\n" +
                           (rawBot ?? string.Empty);

            // Parse positions & classify (off-thread OK)
            var items = Parser.ParseAll(combined);
            var (local, system) = TelemetryHelpers.ClassifyPositions(items);

            var now = DateTime.UtcNow;

            if (local != null) { _lastLocal = local; _lastLocalAt = now; }
            if (system != null) { _lastSystem = system; _lastSystemAt = now; }

            if (CamParse.TryParseCamAngles(combined, out var cam))
            {
                _lastCam = cam;
                _lastCamAt = now;
            }

            // Build strings here (still off-thread)
            string camStr = "N/A";
            if (_lastCam is { } c)
            {
                bool stale = (now - _lastCamAt).TotalSeconds > 2.0;
                camStr = $"{c.X:0.#}°, {c.Y:0.#}°, {c.Z:0.#}°" + (stale ? " (stale)" : "");
            }

            string localStr = TelemetryHelpers.FormatPosShort(_lastLocal);
            if (_lastLocal != null && (now - _lastLocalAt).TotalSeconds > 2.0)
                localStr += " (stale)";

            string sysStr = TelemetryHelpers.FormatPosShort(_lastSystem);
            if (_lastSystem != null && (now - _lastSystemAt).TotalSeconds > 2.0)
                sysStr += " (stale)";

            // Push to UI on the UI thread
            Dispatcher.UIThread.Post(() =>
            {
                _camDirLine?.SetCurrentValue(TextBlock.TextProperty, camStr);
                _localPosLine?.SetCurrentValue(TextBlock.TextProperty, localStr);
                _systemPosLine?.SetCurrentValue(TextBlock.TextProperty, sysStr);
            });
        }
        catch
        {
            // keep loop resilient
        }
    }


    private static string TrimForLog(string s, int n) =>
        string.IsNullOrEmpty(s) ? "" : (s.Length <= n ? s.Replace("\n", " ") : s.Substring(0, n).Replace("\n", " ") + "…");

    private static class Log
    {
        private static readonly string Dir =
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData),
                         "SC-TS3-Directional-Audio");
        private static readonly string PathLog = Path.Combine(Dir, "app.log");
        private static readonly object Gate = new();

        public static string DataPath(string fileName)
        {
            Directory.CreateDirectory(Dir);
            return Path.Combine(Dir, fileName);
        }

        public static void Info(string msg)
        {
            try
            {
                Directory.CreateDirectory(Dir);
                var line = $"{DateTime.Now:HH:mm:ss.fff}  {msg}";
                lock (Gate) File.AppendAllText(PathLog, line + Environment.NewLine);
            }
            catch { }
        }
    }

    // Variants per slice: pick best by (#parsed, then raw length)
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
