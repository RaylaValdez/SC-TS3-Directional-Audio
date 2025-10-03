using OpenCvSharp;
using OpenCvSharp.Extensions;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;
using Size = System.Drawing.Size;

namespace StarCitizenDirectionalAudioOCR;

public sealed class CaptureService
{
    // PUBLIC API --------------------------------------------------------------

    public (int Left, int Top, int Width, int Height)? TryGetStarCitizenRect()
    {
        if (OperatingSystem.IsWindows())
            return FindStarCitizenWindowWindows();
        if (OperatingSystem.IsLinux())
            return FindStarCitizenWindowLinux();
        return null;
    }

    public async Task<(int Left, int Top, int Width, int Height)?> WaitForStarCitizenRectAsync(int pollMs = 400)
    {
        while (true)
        {
            var r = TryGetStarCitizenRect();
            if (r != null) return r;
            await Task.Delay(pollMs);
        }
    }

    public Mat CaptureFullWindow((int Left, int Top, int Width, int Height) r)
    {
        if (OperatingSystem.IsWindows())
        {
            using var bmp = new Bitmap(r.Width, r.Height, System.Drawing.Imaging.PixelFormat.Format32bppArgb);
            using (var g = Graphics.FromImage(bmp))
                g.CopyFromScreen(r.Left, r.Top, 0, 0, new Size(r.Width, r.Height));
            return BitmapConverter.ToMat(bmp);
        }
        else // Linux via ImageMagick `import`
        {
            string tmp = Path.Combine(Path.GetTempPath(), "sc_full.png");
            var cmd = $"import -window root -crop {r.Width}x{r.Height}+{r.Left}+{r.Top} {tmp}";
            Exec($"bash -c \"{cmd}\"");
            return File.Exists(tmp) ? Cv2.ImRead(tmp, ImreadModes.Color) : new Mat();
        }
    }

    public Rect ComputeAbsoluteRoi((int Left, int Top, int Width, int Height) win, Roi roi)
    {
        int ax = win.Left + (int)(win.Width * roi.Left);
        int ay = win.Top + (int)(win.Height * roi.Top);
        int aw = Math.Max(1, (int)(win.Width * roi.Width));
        int ah = Math.Max(1, (int)(win.Height * roi.Height));
        return new Rect(ax, ay, aw, ah);
    }

    public Mat CaptureScreenRect(Rect r)
    {
        if (OperatingSystem.IsWindows())
        {
            using var bmp = new Bitmap(r.Width, r.Height, System.Drawing.Imaging.PixelFormat.Format32bppArgb);
            using (var g = Graphics.FromImage(bmp))
                g.CopyFromScreen(r.X, r.Y, 0, 0, new Size(r.Width, r.Height));
            return BitmapConverter.ToMat(bmp);
        }
        else // Linux via ImageMagick `import`
        {
            string tmp = Path.Combine(Path.GetTempPath(), "sc_roi.png");
            var cmd = $"import -window root -crop {r.Width}x{r.Height}+{r.X}+{r.Y} {tmp}";
            Exec($"bash -c \"{cmd}\"");
            return File.Exists(tmp) ? Cv2.ImRead(tmp, ImreadModes.Color) : new Mat();
        }
    }

    public Mat CropByFractions(Mat full, Roi roi)
    {
        int x = (int)(full.Width * roi.Left);
        int y = (int)(full.Height * roi.Top);
        int w = Math.Max(1, (int)(full.Width * roi.Width));
        int h = Math.Max(1, (int)(full.Height * roi.Height));
        w = Math.Min(w, full.Width - x);
        h = Math.Min(h, full.Height - y);
        return new Mat(full, new Rect(x, y, w, h));
    }

    // WINDOWS ----------------------------------------------------------------
    private (int Left, int Top, int Width, int Height)? FindStarCitizenWindowWindows()
    {
        var targets = new[] { "star citizen", "star citizen ptu", "star citizen evocati", "sc alpha" };
        var matches = new List<(IntPtr hWnd, string title, RECT rect)>();

        EnumWindows((hWnd, lParam) =>
        {
            if (!IsWindowVisible(hWnd)) return true;
            int len = GetWindowTextLength(hWnd);
            if (len <= 0) return true;
            var sb = new StringBuilder(len + 1);
            GetWindowText(hWnd, sb, sb.Capacity);
            var title = sb.ToString();
            if (string.IsNullOrWhiteSpace(title)) return true;

            var lower = title.ToLowerInvariant();
            if (targets.Any(t => lower.Contains(t)))
            {
                if (GetWindowRect(hWnd, out RECT r))
                    matches.Add((hWnd, title, r));
            }
            return true;
        }, IntPtr.Zero);

        var best = matches
            .Select(m => (m.rect.Left, m.rect.Top, Width: m.rect.Right - m.rect.Left, Height: m.rect.Bottom - m.rect.Top))
            .Where(r => r.Width > 100 && r.Height > 100)
            .OrderByDescending(r => r.Width * r.Height)
            .FirstOrDefault();

        return best.Width > 0 ? best : null;
    }

    private delegate bool EnumWindowsProc(IntPtr hWnd, IntPtr lParam);
    [DllImport("user32.dll")] private static extern bool EnumWindows(EnumWindowsProc lpEnumFunc, IntPtr lParam);
    [DllImport("user32.dll")] private static extern bool IsWindowVisible(IntPtr hWnd);
    [DllImport("user32.dll")] private static extern int GetWindowTextLength(IntPtr hWnd);
    [DllImport("user32.dll")] private static extern int GetWindowText(IntPtr hWnd, StringBuilder lpString, int nMaxCount);
    [DllImport("user32.dll")] private static extern bool GetWindowRect(IntPtr hWnd, out RECT lpRect);
    private struct RECT { public int Left, Top, Right, Bottom; }

    // LINUX ------------------------------------------------------------------
    private (int Left, int Top, int Width, int Height)? FindStarCitizenWindowLinux()
    {
        try
        {
            string list = Exec("bash -c \"wmctrl -lx || echo _NOWMCTRL_\"");
            var targets = new[] { "star citizen", "star-citizen", "starcitizen" };

            if (!list.Contains("_NOWMCTRL_"))
            {
                foreach (var line in list.Split('\n'))
                {
                    if (targets.Any(t => line.ToLowerInvariant().Contains(t)))
                    {
                        var id = line.Split(' ', StringSplitOptions.RemoveEmptyEntries).FirstOrDefault();
                        if (!string.IsNullOrEmpty(id))
                        {
                            string geom = Exec($"bash -c \"xwininfo -id {id} | egrep 'Absolute upper-left X|Absolute upper-left Y|Width:|Height:'\"");
                            int x = 0, y = 0, w = 0, h = 0;
                            foreach (var ln in geom.Split('\n'))
                            {
                                if (ln.Contains("Absolute upper-left X")) int.TryParse(ln.Split(':')[1], out x);
                                if (ln.Contains("Absolute upper-left Y")) int.TryParse(ln.Split(':')[1], out y);
                                if (ln.Contains("Width:")) int.TryParse(ln.Split(':')[1], out w);
                                if (ln.Contains("Height:")) int.TryParse(ln.Split(':')[1], out h);
                            }
                            if (w > 100 && h > 100) return (x, y, w, h);
                        }
                    }
                }
            }
            else
            {
                string ids = Exec("bash -c \"xdotool search --name 'Star Citizen' || true\"");
                foreach (var id in ids.Split('\n', StringSplitOptions.RemoveEmptyEntries))
                {
                    string geom = Exec($"bash -c \"xdotool getwindowgeometry --shell {id}\"");
                    int x = 0, y = 0, w = 0, h = 0;
                    foreach (var ln in geom.Split('\n'))
                    {
                        if (ln.StartsWith("X=")) int.TryParse(ln[2..], out x);
                        if (ln.StartsWith("Y=")) int.TryParse(ln[2..], out y);
                        if (ln.StartsWith("WIDTH=")) int.TryParse(ln[6..], out w);
                        if (ln.StartsWith("HEIGHT=")) int.TryParse(ln[7..], out h);
                    }
                    if (w > 100 && h > 100) return (x, y, w, h);
                }
            }
        }
        catch { }
        return null;
    }

    private static string Exec(string cmd)
    {
        var psi = new ProcessStartInfo("bash", "-c \"" + cmd + "\"")
        {
            RedirectStandardOutput = true,
            RedirectStandardError = true,
            UseShellExecute = false,
            CreateNoWindow = true
        };
        using var p = Process.Start(psi)!;
        string s = p.StandardOutput.ReadToEnd();
        p.WaitForExit();
        return s;
    }
}
