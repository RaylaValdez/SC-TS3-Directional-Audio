using OpenCvSharp;
using Tesseract;
using System;
using System.IO;

namespace SC_TS3_Directional_Audio.OCR
{
    public static class OCRProcessor
    {
        private static readonly string tessdataPath = Path.Combine(AppContext.BaseDirectory, "TesseractLangData");

        public static Mat Preprocess(Mat mat)
        {
            if (mat.Empty())
                throw new Exception("Input Mat is empty.");

            // --- Determine dynamic crop for top-right lines ---
            int windowWidth = mat.Width;
            int windowHeight = mat.Height;

            // Estimate line height as ~1% of window height
            int lineHeight = Math.Max(15, (int)(windowHeight * 0.015));
            int numLines = 2;
            int cropHeight = lineHeight * numLines;

            // Grab right 50% of window width
            int cropWidth = Math.Max(100, windowWidth / 2);

            // X/Y for top-right
            int x = Math.Max(0, windowWidth - cropWidth);
            int y = 0;

            // Clamp width/height to stay inside the window
            cropWidth = Math.Min(cropWidth, windowWidth - x);
            cropHeight = Math.Min(cropHeight, windowHeight - y);

            if (cropWidth <= 0 || cropHeight <= 0)
            {
                Console.WriteLine("Preprocess returned empty image: ROI out of bounds");
                return new Mat(); // empty Mat
            }

            var roi = new OpenCvSharp.Rect(x, y, cropWidth, cropHeight);
            Mat cropped = new Mat(mat, roi);

            // --- Scale up if too small for Tesseract ---
            const int minWidth = 100;
            const int minHeight = 30;
            int scaleX = Math.Max(1, minWidth / cropped.Width);
            int scaleY = Math.Max(1, minHeight / cropped.Height);
            int scale = Math.Max(scaleX, scaleY);

            if (scale > 1)
            {
                Mat resized = new Mat();
                Cv2.Resize(cropped, resized, new Size(cropped.Width * scale, cropped.Height * scale), 0, 0, InterpolationFlags.Cubic);
                cropped.Dispose();
                cropped = resized;
            }

            // --- Convert to grayscale ---
            Mat matGray = new Mat();
            switch (cropped.Channels())
            {
                case 1: matGray = cropped.Clone(); break;
                case 3: Cv2.CvtColor(cropped, matGray, ColorConversionCodes.BGR2GRAY); break;
                case 4: Cv2.CvtColor(cropped, matGray, ColorConversionCodes.BGRA2GRAY); break;
                default:
                    cropped.Dispose();
                    throw new Exception($"Unsupported channel count: {cropped.Channels()}");
            }

            cropped.Dispose();
            return matGray;
        }


        public static Pix MatToPix(Mat mat)
        {
            Mat matGray;
            if (mat.Channels() == 1)
            {
                matGray = mat.Clone();
            }
            else if (mat.Channels() == 3)
            {
                matGray = new Mat();
                Cv2.CvtColor(mat, matGray, ColorConversionCodes.BGR2GRAY);
            }
            else if (mat.Channels() == 4)
            {
                matGray = new Mat();
                Cv2.CvtColor(mat, matGray, ColorConversionCodes.BGRA2GRAY);
            }
            else
            {
                throw new Exception($"Unsupported channel count: {mat.Channels()}");
            }

            using (matGray)
            {
                Cv2.ImEncode(".png", matGray, out byte[] pngBytes);
                return Pix.LoadFromMemory(pngBytes);
            }
        }

        public static string PerformOCR(Mat mat)
        {
            using var engine = new TesseractEngine(tessdataPath, "eng", EngineMode.Default);
            using Pix pix = MatToPix(mat);

            // Auto page segmentation works better for live multi-line crops
            using Page page = engine.Process(pix, PageSegMode.Auto);
            return page.GetText();
        }
    }
}
