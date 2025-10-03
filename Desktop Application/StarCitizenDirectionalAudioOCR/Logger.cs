using System;
using System.IO;

namespace StarCitizenDirectionalAudioOCR
{
    public static class Logger
    {
        private static readonly string PathLog =
            System.IO.Path.Combine(
                Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData),
                "SC-TS3-Directional-Audio", "app.log");

        private static readonly object Gate = new();

        public static void Info(string msg)
        {
            try
            {
                Directory.CreateDirectory(System.IO.Path.GetDirectoryName(PathLog)!);
                var line = $"{DateTime.Now:HH:mm:ss.fff}  {msg}";
                lock (Gate) File.AppendAllText(PathLog, line + Environment.NewLine);
            }
            catch { /* no-op */ }
        }
    }
}
