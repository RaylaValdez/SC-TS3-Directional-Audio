using Avalonia;
using Avalonia.Controls.ApplicationLifetimes;


namespace StarCitizenDirectionalAudioOCR;

public partial class App : Application
{
    public override void OnFrameworkInitializationCompleted()
    {
        if (ApplicationLifetime is IClassicDesktopStyleApplicationLifetime d)
            d.MainWindow = new MainWindow();   // same namespace as below
        base.OnFrameworkInitializationCompleted();
    }
}
