using Avalonia;

namespace RegionSelector.Desktop
{
    internal static class Program
    {
        public static void Main(string[] args)
        {
            BuildAvaloniaApp().StartWithClassicDesktopLifetime(args);
        }

        public static AppBuilder BuildAvaloniaApp()
            => AppBuilder.Configure<RegionSelector.App>()
                         .UsePlatformDetect()
                         .LogToTrace();
    }
}
