using System;
using OpenCvSharp;

namespace SC_TS3_Directional_Audio.Capture
{
    internal class ScreenCapture
    {
        public static Mat CaptureTest(string path)
        {
            if (!System.IO.File.Exists(path))
                throw new FileNotFoundException($"Test image not found at: {path}");

            Mat mat = Cv2.ImRead(path, ImreadModes.Color);
            if (mat.Empty())
                throw new Exception("Mat failed to load. Check file path or integrity.");
            return mat;
        }

    }
}
