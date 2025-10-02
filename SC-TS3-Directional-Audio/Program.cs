using OpenCvSharp;
using SC_TS3_Directional_Audio.Capture;
using SC_TS3_Directional_Audio.OCR;
using SC_TS3_Directional_Audio.Parsing;
using System.Text.RegularExpressions;
using Tesseract;

class Program
{
    static void Main()
    {
        Console.WriteLine("Star Citizen Pos OCR Test");

        string screenshotPath = Path.Combine(AppContext.BaseDirectory, "Test Image", "test.png");

        Mat mat = ScreenCapture.CaptureTest(screenshotPath);
        if (mat.Empty())
        {
            Console.WriteLine("Image failed to load. Check path and file format.");
            return;
        }

        Mat preprocessed = OCRProcessor.Preprocess(mat, saveDebug: true);  // save debug image

        // Optional: whitelist common SC characters
        string whitelist = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.-_: ";
        string ocrText = OCRProcessor.PerformOCR(preprocessed, charWhitelist: whitelist, pageSegMode: PageSegMode.SingleLine);


        Console.WriteLine("OCR Result:");
        Console.WriteLine(ocrText);
    }
}
