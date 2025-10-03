using System;
using System.IO;
using System.Text.Json;
using System.Threading;

namespace StarCitizenDirectionalAudioOCR;

public record Roi(double Left, double Top, double Width, double Height);

public sealed class RoiStore
{
    private readonly string _path;
    private readonly object _gate = new();

    public RoiStore(string? customPath = null)
    {
        _path = customPath ?? DefaultPath();
        Directory.CreateDirectory(Path.GetDirectoryName(_path)!);
    }

    public Roi LoadOrDefault()
    {
        lock (_gate)
        {
            try
            {
                if (File.Exists(_path))
                {
                    var json = File.ReadAllText(_path);
                    var r = JsonSerializer.Deserialize<Roi>(json);
                    if (r != null) return Clamp(r);
                }
            }
            catch { /* ignore, fall back to default */ }
            // Default: small strip top-right
            return new Roi(0.70, 0.04, 0.28, 0.06);
        }
    }

    public void Save(Roi roi)
    {
        roi = Clamp(roi);
        var json = JsonSerializer.Serialize(roi, new JsonSerializerOptions { WriteIndented = true });
        lock (_gate)
        {
            AtomicWrite(_path, json);
        }
    }

    private static void AtomicWrite(string path, string content)
    {
        var dir = Path.GetDirectoryName(path)!;
        var tmp = Path.Combine(dir, Path.GetFileName(path) + ".tmp");
        File.WriteAllText(tmp, content);
        // Replace if exists; if not, move is fine.
        if (File.Exists(path))
            File.Replace(tmp, path, null);
        else
            File.Move(tmp, path);
    }

    private static Roi Clamp(Roi r)
    {
        double L = Math.Clamp(r.Left, 0, 1);
        double T = Math.Clamp(r.Top, 0, 1);
        double W = Math.Clamp(r.Width, 1e-4, 1 - L);
        double H = Math.Clamp(r.Height, 1e-4, 1 - T);
        return new Roi(L, T, W, H);
    }

    private static string DefaultPath()
    {
        var baseDir = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData),
            "SC-TS3-Directional-Audio");
        return Path.Combine(baseDir, "roi.json");
        // On Linux this maps to ~/.config/…, on Windows to %APPDATA%\…
    }
}