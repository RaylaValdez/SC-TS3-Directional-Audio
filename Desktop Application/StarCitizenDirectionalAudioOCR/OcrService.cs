using OpenCvSharp;
using System;
using System.Collections.Generic;
using System.IO;
using Tesseract;

namespace StarCitizenDirectionalAudioOCR;

public sealed class OcrService
{
    private readonly string _tess = Path.Combine(AppContext.BaseDirectory, "TesseractLangData");

    public TesseractEngine CreateEngine(out string info)
    {
        if (!Directory.Exists(_tess))
        {
            info = $"tessdata missing at {_tess}";
            throw new DirectoryNotFoundException(info);
        }
        var files = Directory.GetFiles(_tess, "*.traineddata");
        info = $"tessdata='{_tess}', langs={files.Length}";
        var eng = new TesseractEngine(_tess, "eng", EngineMode.Default);

        // HUD letters, digits, punctuation we expect (includes underscore).
        eng.SetVariable("tessedit_char_whitelist",
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ:_-+., mKM");
        eng.SetVariable("preserve_interword_spaces", "1");
        eng.SetVariable("tessedit_do_invert", "1");
        eng.DefaultPageSegMode = PageSegMode.SingleLine;
        return eng;
    }

    // --- Helpers -------------------------------------------------------------

    private static Mat ToGray(Mat src)
        => src.Channels() == 1 ? src.Clone() : src.CvtColor(ColorConversionCodes.BGR2GRAY);

    private static Mat UpscaleTo(Mat srcGray, int targetH, out bool upscaled)
    {
        upscaled = false;
        if (srcGray.Rows >= targetH) return srcGray.Clone();
        var dst = new Mat();
        double s = targetH / (double)srcGray.Rows;
        Cv2.Resize(srcGray, dst, new Size(srcGray.Cols * s, targetH), 0, 0, InterpolationFlags.Cubic);
        upscaled = true;
        return dst;
    }

    private static Mat Clahe(Mat g)
    {
        using var clahe = Cv2.CreateCLAHE(clipLimit: 2.0, tileGridSize: new Size(8, 8));
        var dst = new Mat();
        clahe.Apply(g, dst);
        return dst;
    }

    private static Mat WhiteTopHat(Mat g, int w = 25, int h = 3)
    {
        // Remove slow-varying bright background; keep small bright text
        using var kernel = Cv2.GetStructuringElement(MorphShapes.Rect, new Size(w, h));
        var dst = new Mat();
        Cv2.MorphologyEx(g, dst, MorphTypes.TopHat, kernel);
        return dst;
    }

    private static Mat Adaptive(Mat g)
    {
        var dst = new Mat();
        // Gaussian adaptive works well against busy backgrounds; block size odd
        Cv2.AdaptiveThreshold(g, dst, 255, AdaptiveThresholdTypes.GaussianC, ThresholdTypes.Binary, 31, 2);
        return dst;
    }

    private static Mat Otsu(Mat g)
    {
        var blurred = new Mat();
        Cv2.GaussianBlur(g, blurred, new Size(3, 3), 0);
        var dst = new Mat();
        Cv2.Threshold(blurred, dst, 0, 255, ThresholdTypes.Binary | ThresholdTypes.Otsu);
        blurred.Dispose();
        return dst;
    }

    private static Mat CloseThin(Mat bw)
    {
        using var k = Cv2.GetStructuringElement(MorphShapes.Rect, new Size(2, 1));
        var dst = new Mat();
        Cv2.MorphologyEx(bw, dst, MorphTypes.Close, k);
        return dst;
    }

    private static Mat EnsureDarkText(Mat bw)
    {
        // Make text dark on light background: better for Tesseract
        var mean = Cv2.Mean(bw).Val0;
        if (mean < 128)
        {
            var inv = new Mat();
            Cv2.BitwiseNot(bw, inv);
            return inv;
        }
        return bw.Clone();
    }

    // Build a small set of variants tailored for bright text over clutter.
    // Each result is a 1-channel Mat suitable for OCR.
    public List<(string tag, Mat mat)> PreprocessVariants(Mat src)
    {
        var list = new List<(string, Mat)>();

        using var gray0 = ToGray(src);
        using var gray = UpscaleTo(gray0, targetH: Math.Clamp(gray0.Rows * 2, 80, 140), out _);

        // Variant A: CLAHE -> Otsu -> close -> ensure dark text
        using (var a1 = Clahe(gray))
        using (var a2 = Otsu(a1))
        using (var a3 = CloseThin(a2))
        using (var a4 = EnsureDarkText(a3))
            list.Add(("clahe+otsu", a4.Clone()));

        // Variant B: White Top-hat (background removal) -> Adaptive -> close -> ensure dark
        using (var b1 = WhiteTopHat(gray))
        using (var b2 = Adaptive(b1))
        using (var b3 = CloseThin(b2))
        using (var b4 = EnsureDarkText(b3))
            list.Add(("tophat+adapt", b4.Clone()));

        // Variant C: Adaptive on CLAHE (sometimes better than A/B)
        using (var c1 = Clahe(gray))
        using (var c2 = Adaptive(c1))
        using (var c3 = CloseThin(c2))
        using (var c4 = EnsureDarkText(c3))
            list.Add(("clahe+adapt", c4.Clone()));

        // Variant D: Gray upscaled only (no binarize) – occasionally wins
        list.Add(("gray", gray.Clone()));

        return list;
    }

    public string Run(TesseractEngine engine, Mat mat)
    {
        Cv2.ImEncode(".png", mat, out var bytes, new[] { (int)ImwriteFlags.PngCompression, 1 });
        using var pix = Pix.LoadFromMemory(bytes);
        using var page = engine.Process(pix, PageSegMode.SingleLine);
        return page.GetText() ?? string.Empty;
    }
}
