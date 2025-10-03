using OpenCvSharp;
using SC_TS3_Directional_Audio.Capture;
using SC_TS3_Directional_Audio.OCR;
using SC_TS3_Directional_Audio.Parsing;
using System;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Text.Json;
using System.Threading;

record Roi(double Left, double Top, double Width, double Height);

class Program
{
    static void Main(string[] args)
    {
        Console.WriteLine("Star Citizen Live Pos OCR");
        Console.WriteLine("Press Ctrl+C to exit.");

        // One-shot: run overlay to select ROI
        if (args.Contains("--select-region"))
        {
            var rect = ScreenCapture.GetStarCitizenWindowRect();
            if (rect is null) { Console.WriteLine("Star Citizen not focused / window not found."); return; }

            var outPath = Path.Combine(AppContext.BaseDirectory, "ocr_region.json");
            RunRegionSelector(rect.Value.Left, rect.Value.Top, rect.Value.Width, rect.Value.Height, outPath);
            Console.WriteLine($"Saved ROI to {outPath}");
            return;
        }

        // Params
        int tps = 2; // capture Hz
        int tickDelayMs = Math.Max(1, 1000 / tps);
        int ocrIntervalMs = 800;
        bool binarize = false;

        string roiPath = Path.Combine(AppContext.BaseDirectory, "ocr_region.json");
        Roi roiFractions = LoadRoiOrDefault(roiPath);

        // Reuse Tesseract engine
        using var engine = new Tesseract.TesseractEngine(
            Path.Combine(AppContext.BaseDirectory, "TesseractLangData"),
            "eng",
            Tesseract.EngineMode.Default
        );
        engine.SetVariable("tessedit_char_whitelist", "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ:.- km");

        Stopwatch ocrTimer = Stopwatch.StartNew();
        bool saidNotFocused = false;

        while (true)
        {
            Mat full = ScreenCapture.CaptureFullWindowIfStarCitizenFocused();
            if (full.Empty())
            {
                if (!saidNotFocused)
                {
                    Console.WriteLine("Skipping ticks: Star Citizen not focused or capture failed.");
                    saidNotFocused = true;
                }
                Thread.Sleep(tickDelayMs);
                continue;
            }
            saidNotFocused = false;

            // Crop by saved fractions
            int x = (int)(full.Width * roiFractions.Left);
            int y = (int)(full.Height * roiFractions.Top);
            int w = (int)(full.Width * roiFractions.Width);
            int h = (int)(full.Height * roiFractions.Height);
            w = Math.Max(1, Math.Min(w, full.Width - x));
            h = Math.Max(1, Math.Min(h, full.Height - y));

            using var hud = new Mat(full, new OpenCvSharp.Rect(x, y, w, h));
            full.Dispose();

            using var pre = OCRProcessor.Preprocess(hud, binarize);

            if (pre.Empty())
            {
                Thread.Sleep(tickDelayMs);
                continue;
            }

            if (ocrTimer.ElapsedMilliseconds >= ocrIntervalMs)
            {
                try
                {
                    using var pix = OCRProcessor.MatToPix(pre);
                    using var page = engine.Process(pix, Tesseract.PageSegMode.Auto);
                    string text = page.GetText()?.Trim() ?? string.Empty;

                    foreach (var line in text.Split('\n'))
                    {
                        if (SCPositionParser.TryParseZoneAndPosition(line, out string zone, out double xKm, out double yKm, out double zKm))
                        {
                            Console.WriteLine($"[{DateTime.Now:HH:mm:ss}] Zone: {zone}, Pos: {xKm}km {yKm}km {zKm}km");
                            break;
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"OCR Error: {ex.Message}");
                }
                ocrTimer.Restart();
            }

            Thread.Sleep(tickDelayMs);
        }
    }

    static Roi LoadRoiOrDefault(string path)
    {
        var def = new Roi(0.50, 0.007, 0.50, 0.015);
        if (!File.Exists(path)) return def;
        try
        {
            var json = File.ReadAllText(path);
            var r = JsonSerializer.Deserialize<Roi>(json);
            if (r is null) return def;

            double L = Math.Clamp(r.Left, 0, 1);
            double T = Math.Clamp(r.Top, 0, 1);
            double W = Math.Clamp(r.Width, 0, 1 - L);
            double H = Math.Clamp(r.Height, 0, 1 - T);
            return new Roi(L, T, W, H);
        }
        catch { return def; }
    }

    static void RunRegionSelector(int x, int y, int w, int h, string outPath)
    {
#if WINDOWS
        var exe = Path.GetFullPath(Path.Combine(
            AppContext.BaseDirectory, @"..\..\..\..\RegionSelector.Desktop\bin\Debug\net8.0\RegionSelector.Desktop.exe"));
#else
        var exe = Path.GetFullPath(Path.Combine(
            AppContext.BaseDirectory, "../../../../RegionSelector.Desktop/bin/Debug/net8.0/RegionSelector.Desktop"));
#endif
        var psi = new System.Diagnostics.ProcessStartInfo
        {
            FileName = exe,
            Arguments = $"--x={x} --y={y} --w={w} --h={h} --out=\"{outPath}\"",
            UseShellExecute = false
        };
        using var p = System.Diagnostics.Process.Start(psi);
        p?.WaitForExit();
    }
}
