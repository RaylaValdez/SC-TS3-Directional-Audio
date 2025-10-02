using OpenCvSharp;
using Tesseract;
using System;
using SC_TS3_Directional_Audio.Capture;

namespace SC_TS3_Directional_Audio.OCR
{
    public static class OCRProcessor
    {
        private static readonly string tessdataPath = Path.Combine(AppContext.BaseDirectory, "TesseractLangData");

        public static Mat Preprocess(Mat mat, bool saveDebug = true)
        {
            if (mat.Empty())
                throw new Exception("Input Mat is empty.");

            // --- Crop top-right dynamically ---
            double topFraction = 0.007;
            double rightFraction = 0.5;
            double heightFraction = 0.015;

            int cropWidth = (int)(mat.Width * rightFraction);
            int cropHeight = (int)(mat.Height * heightFraction);
            int x = mat.Width - cropWidth;
            int y = (int)(mat.Height * topFraction);

            OpenCvSharp.Rect roi = new OpenCvSharp.Rect(x, y, cropWidth, cropHeight);
            Mat cropped = new Mat(mat, roi);

            // --- Scale if too small ---
            int minWidth = 100;
            int minHeight = 30;

            int scaleX = Math.Max(1, minWidth / cropped.Width);
            int scaleY = Math.Max(1, minHeight / cropped.Height);
            int scale = Math.Max(scaleX, scaleY);

            if (scale > 1)
            {
                Mat resized = new Mat();
                Cv2.Resize(cropped, resized, new OpenCvSharp.Size(cropped.Width * scale, cropped.Height * scale), 0, 0, InterpolationFlags.Cubic);
                cropped = resized;
            }

            // --- Grayscale conversion ---
            Mat matGray = new Mat();
            switch (cropped.Channels())
            {
                case 1: matGray = cropped.Clone(); break;
                case 3: Cv2.CvtColor(cropped, matGray, ColorConversionCodes.BGR2GRAY); break;
                case 4: Cv2.CvtColor(cropped, matGray, ColorConversionCodes.BGRA2GRAY); break;
            }

            // --- Optional debug save ---
            if (saveDebug)
            {
                string debugPath = Path.Combine(AppContext.BaseDirectory, "preprocessed_debug.png");
                Cv2.ImWrite(debugPath, matGray);
                Console.WriteLine($"Preprocessed debug image saved to: {debugPath}");
            }

            return matGray;
        }





        /// <summary>
        /// Converts a Mat to a Tesseract Pix object
        /// </summary>
        private static Pix MatToPix(Mat mat)
        {
            Mat matGray = new Mat();
            if (mat.Channels() == 3)
            {
                Cv2.CvtColor(mat, matGray, ColorConversionCodes.BGR2GRAY);
            }
            else if (mat.Channels() == 4)
            {
                Cv2.CvtColor(mat, matGray, ColorConversionCodes.BGRA2GRAY);
            }
            else if (mat.Channels() == 1)
            {
                matGray = mat.Clone(); // already grayscale
            }
            else
            {
                throw new Exception($"Unsupported number of channels: {mat.Channels()}");
            }
            // Encode Mat to PNG bytes
            Cv2.ImEncode(".png", matGray, out byte[] pngBytes);

            // Load into Pix
            return Pix.LoadFromMemory(pngBytes);
        }

        public static string PerformOCR(Mat mat, string charWhitelist = null, PageSegMode pageSegMode = PageSegMode.SingleLine)
        {
            using var engine = new TesseractEngine(tessdataPath, "eng", EngineMode.Default);

            // Optional: set character whitelist if provided
            if (!string.IsNullOrEmpty(charWhitelist))
            {
                engine.SetVariable("tessedit_char_whitelist", charWhitelist);
            }

            using Pix pix = MatToPix(mat);
            using Page page = engine.Process(pix, pageSegMode);
            return page.GetText();
        }

    }
}
