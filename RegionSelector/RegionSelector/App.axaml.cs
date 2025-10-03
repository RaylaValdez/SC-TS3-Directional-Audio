using Avalonia;
using Avalonia.Controls.ApplicationLifetimes;
using System;

namespace RegionSelector
{
    public partial class App : Application
    {
        public override void OnFrameworkInitializationCompleted()
        {
            if (ApplicationLifetime is IClassicDesktopStyleApplicationLifetime d)
            {
                // Parse args: --x= --y= --w= --h= --out=...
                int x = 0, y = 0, w = 0, h = 0;
                string outPath = System.IO.Path.Combine(AppContext.BaseDirectory, "ocr_region.json");
                foreach (var a in d.Args ?? [])
                {
                    var kv = a.Split('=', 2);
                    if (kv.Length != 2) continue;
                    switch (kv[0])
                    {
                        case "--x": int.TryParse(kv[1], out x); break;
                        case "--y": int.TryParse(kv[1], out y); break;
                        case "--w": int.TryParse(kv[1], out w); break;
                        case "--h": int.TryParse(kv[1], out h); break;
                        case "--out": outPath = kv[1]; break;
                    }
                }

                d.MainWindow = new Views.MainWindow(x, y, w, h, outPath);
            }

            base.OnFrameworkInitializationCompleted();
        }
    }
}
