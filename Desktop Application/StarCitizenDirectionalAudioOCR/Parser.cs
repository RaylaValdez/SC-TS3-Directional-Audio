using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;

namespace StarCitizenDirectionalAudioOCR;

public record ParsedPos(string Zone, double X_m, double Y_m, double Z_m, string Raw);

public static class Parser
{
    private static readonly Regex Rx = new Regex(
        @"Zo?ne\s*[:;]\s*(?<zone>.+?)\s+" +
        @"Po?s\s*[:;]\s*" +
        @"(?<x>[-−]?\d+(?:[.,]\d+)?)\s*(?<ux>[kK]?[mM])\W+" +
        @"(?<y>[-−]?\d+(?:[.,]\d+)?)\s*(?<uy>[kK]?[mM])\W+" +
        @"(?<z>[-−]?\d+(?:[.,]\d+)?)\s*(?<uz>[kK]?[mM])\b",
        RegexOptions.Compiled | RegexOptions.IgnoreCase);

    private static double ToMeters(string value, string unit)
    {
        var v = value.Replace(',', '.');
        if (!double.TryParse(v, NumberStyles.Float, CultureInfo.InvariantCulture, out var d))
            return double.NaN;
        return (unit.Trim().Equals("km", StringComparison.OrdinalIgnoreCase)) ? d * 1000.0 : d;
    }

    private static string Clean(string text)
    {
        if (string.IsNullOrWhiteSpace(text)) return string.Empty;
        var sb = new StringBuilder(text.Length);
        foreach (var ch in text)
            sb.Append(ch == '−' ? '-' : ch);
        return sb.ToString();
    }

    public static List<ParsedPos> ParseAll(string text)
    {
        var cleaned = Clean(text);
        var list = new List<ParsedPos>();
        foreach (Match m in Rx.Matches(cleaned))
        {
            var zone = (m.Groups["zone"].Value ?? "").Trim();
            var x = ToMeters(m.Groups["x"].Value, m.Groups["ux"].Value);
            var y = ToMeters(m.Groups["y"].Value, m.Groups["uy"].Value);
            var z = ToMeters(m.Groups["z"].Value, m.Groups["uz"].Value);
            if (double.IsFinite(x) && double.IsFinite(y) && double.IsFinite(z))
                list.Add(new ParsedPos(zone, x, y, z, m.Value));
        }
        return list;
    }

    public static string FormatForDisplay(IEnumerable<ParsedPos> items)
    {
        if (items == null) return string.Empty;
        var best = items.FirstOrDefault();
        if (best is null) return string.Empty;

        static (double v, string unit) Pretty(double meters)
            => Math.Abs(meters) >= 10000 ? (meters / 1000.0, "km") : (meters, "m");

        var (px, ux) = Pretty(best.X_m);
        var (py, uy) = Pretty(best.Y_m);
        var (pz, uz) = Pretty(best.Z_m);

        return $"Zone: {best.Zone}  Pos: {px:0.###} {ux} {py:0.###} {uy} {pz:0.###} {uz}";
    }
}

public static class TelemetryHelpers
{
    public static (ParsedPos? local, ParsedPos? system) ClassifyPositions(IEnumerable<ParsedPos> items)
    {
        ParsedPos? local = null, system = null;

        foreach (var p in items)
        {
            var mag = Math.Max(Math.Abs(p.X_m), Math.Max(Math.Abs(p.Y_m), Math.Abs(p.Z_m)));
            bool looksLocal =
                mag < 10_000 ||
                p.Zone.Contains("ObjectContainer", StringComparison.OrdinalIgnoreCase) ||
                p.Zone.Contains("hab", StringComparison.OrdinalIgnoreCase);

            bool looksSystem =
                !looksLocal ||
                p.Zone.Contains("OOC_", StringComparison.OrdinalIgnoreCase);

            if (looksLocal && local is null) local = p;
            else if (looksSystem && system is null) system = p;
            else if (system is null) system = p;
        }
        return (local, system);
    }

    public static string FormatPosShort(ParsedPos? p)
    {
        if (p is null) return "N/A";

        static (double v, string unit) Pretty(double meters)
            => Math.Abs(meters) >= 10_000 ? (meters / 1000.0, "km") : (meters, "m");

        var (x, ux) = Pretty(p.X_m);
        var (y, uy) = Pretty(p.Y_m);
        var (z, uz) = Pretty(p.Z_m);
        return $"{x:0.###} {ux}, {y:0.###} {uy}, {z:0.###} {uz}";
    }
}

/// Camera-angle parser (degrees), resilient to OCR noise.
/// Finds any line mentioning "cam" and grabs the first 3-angle window with |deg|<=360.
public static class CamParse
{
    public static bool TryParseCamAngles(string text, out (double X, double Y, double Z)? camDeg)
    {
        camDeg = null;
        if (string.IsNullOrWhiteSpace(text)) return false;

        string? line = text.Split('\n')
            .FirstOrDefault(l => l.IndexOf("cam", StringComparison.OrdinalIgnoreCase) >= 0);
        if (line is null) return false;

        line = line.Replace('\u2212', '-').Replace(',', '.');

        var matches = Regex.Matches(line, @"-?\d+(?:\.\d+)?");
        if (matches.Count < 3) return false;

        static double D(string s) =>
            double.TryParse(s, NumberStyles.Float, CultureInfo.InvariantCulture, out var v) ? v : 0.0;

        var nums = matches.Select(m => D(m.Value)).ToList();

        for (int i = 0; i <= nums.Count - 3; i++)
        {
            var a = nums[i]; var b = nums[i + 1]; var c = nums[i + 2];
            if (Math.Abs(a) <= 360 && Math.Abs(b) <= 360 && Math.Abs(c) <= 360)
            { camDeg = (a, b, c); return true; }
        }
        camDeg = (nums[0], nums[1], nums[2]);
        return true;
    }
}
