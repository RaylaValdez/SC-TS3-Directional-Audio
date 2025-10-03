using OpenCvSharp;
using Tesseract;
using System;
using System.IO;

namespace SC_TS3_Directional_Audio.OCR
{
    /// <summary>
    /// Takes an already-cropped HUD strip (from ScreenCapture), scales if needed,
    /// converts to grayscale (and optionally binarizes), then OCRs.
    /// </summary>
    public static class OCRProcessor
    {
        private static readonly string TessdataPath =
            Path.Combine(AppContext.BaseDirectory, "TesseractLangData");

        // Minimum size so Tesseract has enough pixels to chew on
        public const int MinOcrWidth = 100;
        public const int MinOcrHeight = 30;

        /// <summary>
        /// Prepare an *already-cropped* HUD image for OCR:
        /// - upscale to minimum size
        /// - grayscale
        /// - optional binarization (helps when bloom/HDR is rough)
        /// </summary>
        public static Mat Preprocess(Mat hudCrop, bool binarize = false)
        {
            if (hudCrop is null || hudCrop.Empty())
                return new Mat();

            // --- Scale up if needed ---
            int scaleX = Math.Max(1, MinOcrWidth / Math.Max(hudCrop.Width, 1));
            int scaleY = Math.Max(1, MinOcrHeight / Math.Max(hudCrop.Height, 1));
            int scale = Math.Max(scaleX, scaleY);

            Mat work;
            if (scale > 1)
            {
                work = new Mat();
                Cv2.Resize(hudCrop, work,
                    new Size(hudCrop.Width * scale, hudCrop.Height * scale),
                    0, 0, InterpolationFlags.Cubic);
            }
            else
            {
                work = hudCrop.Clone();
            }

            // --- Grayscale ---
            if (work.Channels() != 1)
            {
                var gray = new Mat();
                if (work.Channels() == 3)
                    Cv2.CvtColor(work, gray, ColorConversionCodes.BGR2GRAY);
                else if (work.Channels() == 4)
                    Cv2.CvtColor(work, gray, ColorConversionCodes.BGRA2GRAY);
                else { work.Dispose(); return new Mat(); }

                work.Dispose();
                work = gray;
            }

            // --- Optional binarization (robustness toggle) ---
            if (binarize)
            {
                // Light CLAHE + Otsu threshold for clarity
                using var clahe = Cv2.CreateCLAHE(clipLimit: 2.0, new Size(8, 8));
                var eq = new Mat();
                clahe.Apply(work, eq);
                work.Dispose();

                var th = new Mat();
                Cv2.Threshold(eq, th, 0, 255, ThresholdTypes.Binary | ThresholdTypes.Otsu);
                eq.Dispose();
                work = th;
            }

            return work;
        }

        /// <summary> Convert grayscale Mat to Tesseract Pix via PNG bytes. </summary>
        public static Pix MatToPix(Mat mat)
        {
            if (mat is null || mat.Empty())
                throw new ArgumentException("MatToPix: empty input");

            if (mat.Channels() != 1)
                throw new ArgumentException("MatToPix: expect grayscale Mat (run Preprocess first)");

            using (mat)
            {
                Cv2.ImEncode(".png", mat, out byte[] png);
                return Pix.LoadFromMemory(png);
            }
        }

        /// <summary> OCR the preprocessed (grayscale) Mat and return raw text. </summary>
        public static string PerformOCR(Mat preprocessed)
        {
            if (preprocessed is null || preprocessed.Empty())
                return string.Empty;

            using var engine = new TesseractEngine(TessdataPath, "eng", EngineMode.Default);
            using var pix = MatToPix(preprocessed);
            using var page = engine.Process(pix, PageSegMode.Auto);
            return page.GetText() ?? string.Empty;
        }
    }
}
