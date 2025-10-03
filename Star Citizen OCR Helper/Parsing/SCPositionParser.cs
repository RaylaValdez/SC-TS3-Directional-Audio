using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;

namespace SC_TS3_Directional_Audio.Parsing
{
    public static class SCPositionParser
    {
        public static bool TryParseZoneAndPosition(string line, out string zone, out double x, out double y, out double z)
        {
            zone = null;
            x = y = z = 0;
            var match = Regex.Match(line, @"Zone:\s*(\S+)\s+Pos:\s*(-?\d+\.?\d*)km\s*(-?\d+\.?\d*)km\s*(-?\d+\.?\d*)km");
            if (!match.Success) return false;

            zone = match.Groups[1].Value;
            x = double.Parse(match.Groups[2].Value);
            y = double.Parse(match.Groups[3].Value);
            z = double.Parse(match.Groups[4].Value);
            return true;
        }
    }
}
