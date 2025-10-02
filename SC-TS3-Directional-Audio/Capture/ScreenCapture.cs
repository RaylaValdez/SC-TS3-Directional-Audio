using System;
using System.Diagnostics;
using System.Drawing;
using System.Runtime.InteropServices;
using System.Text;
using OpenCvSharp;
using OpenCvSharp.Extensions;
using System.IO;

namespace SC_TS3_Directional_Audio.Capture
{
    internal class ScreenCapture
    {
        /// <summary>
        /// Capture the top-right OCR region of Star Citizen if focused, empty Mat otherwise
        /// </summary>
        public static Mat CaptureTopRightRegionIfStarCitizenFocused()
        {
            if (OperatingSystem.IsWindows())
                return CaptureWindowsTopRight();
            else if (OperatingSystem.IsLinux())
                return CaptureLinuxTopRight();
            else
                throw new PlatformNotSupportedException("Screen capture only supported on Windows or Linux.");
        }

        #region Windows

        private static Mat CaptureWindowsTopRight()
        {
            IntPtr handle = GetForegroundWindow();
            if (!IsStarCitizenWindow(handle))
                return new Mat(); // empty

            if (!GetWindowRect(handle, out RECT rect))
                return new Mat();

            int width = rect.Right - rect.Left;
            int height = rect.Bottom - rect.Top;
            if (width <= 0 || height <= 0)
                return new Mat();

            // --- Crop fractions for top-right region (tune as needed) ---
            double topFraction = 0.007;
            double rightFraction = 0.5;
            double heightFraction = 0.015;

            int cropWidth = (int)(width * rightFraction);
            int cropHeight = (int)(height * heightFraction);
            int x = rect.Left + width - cropWidth;
            int y = rect.Top + (int)(height * topFraction);

            using Bitmap bmp = new Bitmap(cropWidth, cropHeight, System.Drawing.Imaging.PixelFormat.Format32bppArgb);
            using Graphics g = Graphics.FromImage(bmp);
            g.CopyFromScreen(x, y, 0, 0, new System.Drawing.Size(cropWidth, cropHeight));

            return BitmapConverter.ToMat(bmp);
        }

        [DllImport("user32.dll")]
        private static extern IntPtr GetForegroundWindow();

        [DllImport("user32.dll", SetLastError = true)]
        private static extern int GetWindowText(IntPtr hWnd, StringBuilder lpString, int nMaxCount);

        [DllImport("user32.dll")]
        [return: MarshalAs(UnmanagedType.Bool)]
        private static extern bool GetWindowRect(IntPtr hWnd, out RECT lpRect);

        [StructLayout(LayoutKind.Sequential)]
        private struct RECT { public int Left, Top, Right, Bottom; }

        private static bool IsStarCitizenWindow(IntPtr hWnd)
        {
            if (hWnd == IntPtr.Zero)
                return false;

            const int nChars = 256;
            StringBuilder buff = new StringBuilder(nChars);
            if (GetWindowText(hWnd, buff, nChars) > 0)
                return buff.ToString().Contains("Star Citizen", StringComparison.OrdinalIgnoreCase);

            return false;
        }

        #endregion

        #region Linux

        private static Mat CaptureLinuxTopRight()
        {
            if (!IsStarCitizenFocusedLinux())
                return new Mat();

            var rect = GetStarCitizenWindowRectLinux();
            if (rect == null)
                return new Mat();

            // --- Crop fractions for top-right region ---
            double topFraction = 0.007;
            double rightFraction = 0.5;
            double heightFraction = 0.015;

            int cropWidth = (int)(rect.Width * rightFraction);
            int cropHeight = (int)(rect.Height * heightFraction);
            int x = rect.X + rect.Width - cropWidth;
            int y = rect.Y + (int)(rect.Height * topFraction);

            string tempFile = Path.Combine(Path.GetTempPath(), "sc_capture.png");
            string cmd = $"import -window root -crop {cropWidth}x{cropHeight}+{x}+{y} {tempFile}";

            var psi = new ProcessStartInfo
            {
                FileName = "bash",
                Arguments = $"-c \"{cmd}\"",
                RedirectStandardOutput = true,
                RedirectStandardError = true,
                UseShellExecute = false,
                CreateNoWindow = true
            };

            using var proc = Process.Start(psi);
            proc.WaitForExit();

            if (!File.Exists(tempFile))
                return new Mat();

            return Cv2.ImRead(tempFile, ImreadModes.Color);
        }

        private class RectLinux { public int X, Y, Width, Height; }

        private static bool IsStarCitizenFocusedLinux()
        {
            try
            {
                var psi = new ProcessStartInfo
                {
                    FileName = "xdotool",
                    Arguments = "getwindowfocus getwindowname",
                    RedirectStandardOutput = true,
                    UseShellExecute = false,
                    CreateNoWindow = true
                };
                using var proc = Process.Start(psi);
                string title = proc.StandardOutput.ReadToEnd().Trim();
                proc.WaitForExit();
                return title.Contains("Star Citizen", StringComparison.OrdinalIgnoreCase);
            }
            catch
            {
                return false;
            }
        }

        private static RectLinux? GetStarCitizenWindowRectLinux()
        {
            try
            {
                // Get active window id
                var psiId = new ProcessStartInfo
                {
                    FileName = "xdotool",
                    Arguments = "getactivewindow",
                    RedirectStandardOutput = true,
                    UseShellExecute = false,
                    CreateNoWindow = true
                };
                using var procId = Process.Start(psiId);
                string windowId = procId.StandardOutput.ReadToEnd().Trim();
                procId.WaitForExit();

                // Get geometry
                var psiGeom = new ProcessStartInfo
                {
                    FileName = "xdotool",
                    Arguments = $"getwindowgeometry --shell {windowId}",
                    RedirectStandardOutput = true,
                    UseShellExecute = false,
                    CreateNoWindow = true
                };
                using var procGeom = Process.Start(psiGeom);
                string output = procGeom.StandardOutput.ReadToEnd();
                procGeom.WaitForExit();

                int x = 0, y = 0, w = 0, h = 0;
                foreach (var line in output.Split('\n'))
                {
                    if (line.StartsWith("X=")) x = int.Parse(line.Substring(2));
                    if (line.StartsWith("Y=")) y = int.Parse(line.Substring(2));
                    if (line.StartsWith("WIDTH=")) w = int.Parse(line.Substring(6));
                    if (line.StartsWith("HEIGHT=")) h = int.Parse(line.Substring(7));
                }

                return new RectLinux { X = x, Y = y, Width = w, Height = h };
            }
            catch
            {
                return null;
            }
        }

        #endregion
    }
}
