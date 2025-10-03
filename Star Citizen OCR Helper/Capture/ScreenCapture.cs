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
    internal static class ScreenCapture
    {
        public struct WinRect
        {
            public int Left, Top, Right, Bottom;
            public int Width => Right - Left;
            public int Height => Bottom - Top;
        }

        /// <summary> Returns the SC window rect if focused; null otherwise. </summary>
        public static WinRect? GetStarCitizenWindowRect()
        {
            if (OperatingSystem.IsWindows())
            {
                IntPtr hWnd = GetForegroundWindow();
                if (!IsStarCitizenWindow(hWnd)) return null;
                if (!GetWindowRect(hWnd, out RECT r)) return null;
                return new WinRect { Left = r.Left, Top = r.Top, Right = r.Right, Bottom = r.Bottom };
            }
            else if (OperatingSystem.IsLinux())
            {
                if (!IsStarCitizenFocusedLinux()) return null;
                var r = GetStarCitizenWindowRectLinux();
                if (r == null) return null;
                return new WinRect { Left = r.X, Top = r.Y, Right = r.X + r.Width, Bottom = r.Y + r.Height };
            }
            else return null;
        }

        /// <summary> Captures the **full** SC window if focused; empty Mat otherwise. </summary>
        public static Mat CaptureFullWindowIfStarCitizenFocused()
        {
            if (OperatingSystem.IsWindows())
                return CaptureFullWindows();
            else if (OperatingSystem.IsLinux())
                return CaptureFullLinux();
            else
                return new Mat();
        }

        // ----------------- Windows -----------------
        private static Mat CaptureFullWindows()
        {
            IntPtr hWnd = GetForegroundWindow();
            if (!IsStarCitizenWindow(hWnd)) return new Mat();

            if (!GetWindowRect(hWnd, out RECT rect)) return new Mat();
            int w = rect.Right - rect.Left, h = rect.Bottom - rect.Top;
            if (w <= 0 || h <= 0) return new Mat();

            using var bmp = new Bitmap(w, h, System.Drawing.Imaging.PixelFormat.Format32bppArgb);
            using (var g = Graphics.FromImage(bmp))
            {
                g.CopyFromScreen(rect.Left, rect.Top, 0, 0, new System.Drawing.Size(w, h));
            }
            return BitmapConverter.ToMat(bmp);
        }

        [DllImport("user32.dll")] private static extern IntPtr GetForegroundWindow();
        [DllImport("user32.dll", SetLastError = true)]
        private static extern int GetWindowText(IntPtr hWnd, StringBuilder lpString, int nMaxCount);
        [DllImport("user32.dll")] private static extern bool GetWindowRect(IntPtr hWnd, out RECT lpRect);
        [StructLayout(LayoutKind.Sequential)] private struct RECT { public int Left, Top, Right, Bottom; }

        private static bool IsStarCitizenWindow(IntPtr hWnd)
        {
            if (hWnd == IntPtr.Zero) return false;
            var sb = new StringBuilder(256);
            if (GetWindowText(hWnd, sb, sb.Capacity) > 0)
                return sb.ToString().Contains("Star Citizen", StringComparison.OrdinalIgnoreCase);
            return false;
        }

        // ----------------- Linux -----------------
        private class RectLinux { public int X, Y, Width, Height; }

        private static Mat CaptureFullLinux()
        {
            if (!IsStarCitizenFocusedLinux()) return new Mat();
            var rect = GetStarCitizenWindowRectLinux();
            if (rect == null) return new Mat();

            string tmp = Path.Combine(Path.GetTempPath(), "sc_full.png");
            string cmd = $"import -window root -crop {rect.Width}x{rect.Height}+{rect.X}+{rect.Y} {tmp}";

            var psi = new ProcessStartInfo
            {
                FileName = "bash",
                Arguments = $"-c \"{cmd}\"",
                UseShellExecute = false
            };
            using var p = Process.Start(psi);
            p?.WaitForExit();

            if (!File.Exists(tmp)) return new Mat();
            return Cv2.ImRead(tmp, ImreadModes.Color);
        }

        private static bool IsStarCitizenFocusedLinux()
        {
            try
            {
                var psi = new ProcessStartInfo
                {
                    FileName = "xdotool",
                    Arguments = "getwindowfocus getwindowname",
                    RedirectStandardOutput = true,
                    UseShellExecute = false
                };
                using var p = Process.Start(psi);
                string title = p!.StandardOutput.ReadToEnd().Trim();
                p.WaitForExit();
                return title.Contains("Star Citizen", StringComparison.OrdinalIgnoreCase);
            }
            catch { return false; }
        }

        private static RectLinux? GetStarCitizenWindowRectLinux()
        {
            try
            {
                var psiId = new ProcessStartInfo
                {
                    FileName = "xdotool",
                    Arguments = "getactivewindow",
                    RedirectStandardOutput = true,
                    UseShellExecute = false
                };
                using var p1 = Process.Start(psiId);
                string id = p1!.StandardOutput.ReadToEnd().Trim();
                p1.WaitForExit();

                var psiGeom = new ProcessStartInfo
                {
                    FileName = "xdotool",
                    Arguments = $"getwindowgeometry --shell {id}",
                    RedirectStandardOutput = true,
                    UseShellExecute = false
                };
                using var p2 = Process.Start(psiGeom);
                string output = p2!.StandardOutput.ReadToEnd();
                p2.WaitForExit();

                int x = 0, y = 0, w = 0, h = 0;
                foreach (var line in output.Split('\n'))
                {
                    if (line.StartsWith("X=")) int.TryParse(line[2..], out x);
                    if (line.StartsWith("Y=")) int.TryParse(line[2..], out y);
                    if (line.StartsWith("WIDTH=")) int.TryParse(line[6..], out w);
                    if (line.StartsWith("HEIGHT=")) int.TryParse(line[7..], out h);
                }
                return new RectLinux { X = x, Y = y, Width = w, Height = h };
            }
            catch { return null; }
        }
    }
}
