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
    // Tolerant regex: handles ":" or ";", flexible whitespace, unicode minus, km/m units, and stray punctuation.
    private static readonly Regex Rx = new Regex(
        @"Zo?ne\s*[:;]\s*(?<zone>.+?)\s+" +
        @"Po?s\s*[:;]\s*" +
        @"(?<x>[-−]?\d+(?:[.,]\d+)?)\s*(?<ux>[kK]?[mM])\W+" +
        @"(?<y>[-−]?\d+(?:[.,]\d+)?)\s*(?<uy>[kK]?[mM])\W+" +
        @"(?<z>[-−]?\d+(?:[.,]\d+)?)\s*(?<uz>[kK]?[mM])\b",
        RegexOptions.Compiled | RegexOptions.IgnoreCase);

    private static double ToMeters(string value, string unit)
    {
        // Normalize decimal comma
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
        {
            // Normalize unicode minus to ASCII hyphen-minus
            sb.Append(ch == '−' ? '-' : ch);
        }
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
            {
                list.Add(new ParsedPos(zone, x, y, z, m.Value));
            }
        }
        return list;
    }

    public static string FormatForDisplay(IEnumerable<ParsedPos> items)
    {
        if (items == null) return string.Empty;
        var best = items.FirstOrDefault();
        if (best is null) return string.Empty;

        // Convert back to km if magnitude is large for readability
        static (double v, string unit) Pretty(double meters)
        {
            if (Math.Abs(meters) >= 10000) return (meters / 1000.0, "km");
            return (meters, "m");
        }

        var (px, ux) = Pretty(best.X_m);
        var (py, uy) = Pretty(best.Y_m);
        var (pz, uz) = Pretty(best.Z_m);

        return $"Zone: {best.Zone}  Pos: {px:0.###} {ux} {py:0.###} {uy} {pz:0.###} {uz}";
    }
}