using OpenCvSharp;
using SC_TS3_Directional_Audio.Capture;
using SC_TS3_Directional_Audio.OCR;
using System;
using System.Diagnostics;
using System.Text.RegularExpressions;
using System.Threading;

class Program
{
    static void Main()
    {
        Console.WriteLine("Star Citizen Live Pos OCR");
        Console.WriteLine("Press Ctrl+C to exit.");

        // --- Configurable ---
        int tps = 1;               // screen capture per second
        int tickDelayMs = 1000 / tps;
        int ocrIntervalMs = 800;   // OCR interval (ms)

        bool hasShownNotFocused = false;

        using var engine = new Tesseract.TesseractEngine(
            Path.Combine(AppContext.BaseDirectory, "TesseractLangData"),
            "eng",
            Tesseract.EngineMode.Default
        );
        // whitelist chars
        engine.SetVariable("tessedit_char_whitelist", "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ:.- km");

        Stopwatch ocrTimer = new Stopwatch();
        ocrTimer.Start();

        Regex zonePosRegex = new Regex(@"Zone:\s*(.+?)\s+Pos:\s*([-\d.]+km\s+[-\d.]+km\s+[-\d.]+km)", RegexOptions.IgnoreCase);

        while (true)
        {
            Mat frame = ScreenCapture.CaptureTopRightRegionIfStarCitizenFocused();

            if (frame.Empty())
            {
                if (!hasShownNotFocused)
                {
                    Console.WriteLine("Skipping ticks: Star Citizen not focused or capture failed.");
                    hasShownNotFocused = true;
                }
                Thread.Sleep(tickDelayMs);
                continue;
            }

            hasShownNotFocused = false;

            Mat cropped = OCRProcessor.Preprocess(frame);
            frame.Dispose();

            if (cropped.Empty())
            {
                Console.WriteLine("Preprocess returned empty image, skipping tick.");
                Thread.Sleep(tickDelayMs);
                continue;
            }

            if (ocrTimer.ElapsedMilliseconds >= ocrIntervalMs)
            {
                try
                {
                    using var pix = OCRProcessor.MatToPix(cropped);
                    using var page = engine.Process(pix, Tesseract.PageSegMode.Auto);
                    string ocrText = page.GetText()?.Trim() ?? "";

                    // extract Zone and Pos
                    var match = zonePosRegex.Match(ocrText);
                    if (match.Success)
                    {
                        string zone = match.Groups[1].Value;
                        string pos = match.Groups[2].Value;
                        Console.WriteLine($"[{DateTime.Now:HH:mm:ss}] Zone: {zone}, Pos: {pos}");
                    }

                }
                catch (Exception ex)
                {
                    Console.WriteLine($"OCR Error: {ex.Message}");
                }

                ocrTimer.Restart();
            }

            cropped.Dispose();
            Thread.Sleep(tickDelayMs);
        }
    }
}
