using Avalonia;
using Avalonia.Controls;
using Avalonia.Input;
using Avalonia.Media;
using Avalonia.Markup.Xaml;
using System;

namespace StarCitizenDirectionalAudioOCR;

public sealed partial class RoiOverlayWindow : Avalonia.Controls.Window
{
    private readonly RoiStore _store;
    private readonly Rect _windowRect;

    private Canvas _canvas = default!;
    private Border _sel  = default!;

    private Point _start;
    private Rect  _startRect;
    private Hit   _hit = Hit.None;

    public RoiOverlayWindow(Rect windowRect, RoiStore store)
    {
        _windowRect = windowRect;
        _store = store;
        InitializeComponent();
#if DEBUG
        this.AttachDevTools();
#endif
    }

    private void InitializeComponent()
    {
        AvaloniaXamlLoader.Load(this);

        // Window chrome/position
        Position = new PixelPoint((int)_windowRect.X, (int)_windowRect.Y);
        Width  = _windowRect.Width;
        Height = _windowRect.Height;
        WindowStartupLocation = WindowStartupLocation.Manual;
        TransparencyLevelHint = new[] { WindowTransparencyLevel.Transparent };
        SystemDecorations = SystemDecorations.None;
        Topmost = true;
        CanResize = false;
        Background = Brushes.Transparent;
        Focusable = true;

        // Root layout: Grid with Canvas (for selection) + instruction text overlay
        var grid = new Grid();
        _canvas = new Canvas { Background = Brushes.Transparent, Focusable = true };
        grid.Children.Add(_canvas);

        var instructions = new TextBlock
        {
            Text = "Drag to adjust ROI • Enter = Save   Esc = Cancel",
            FontSize = 16,
            Foreground = Brushes.Lime,
            HorizontalAlignment = Avalonia.Layout.HorizontalAlignment.Center,
            VerticalAlignment = Avalonia.Layout.VerticalAlignment.Bottom,
            Margin = new Thickness(0,0,0,10),
            TextWrapping = TextWrapping.NoWrap
        };
        grid.Children.Add(instructions);

        Content = grid;

        // Initial selection from store
        var r = _store.LoadOrDefault();
        var sel = new Rect(r.Left * Width, r.Top * Height, r.Width * Width, r.Height * Height);

        _sel = new Border
        {
            BorderBrush = Brushes.Lime,
            BorderThickness = new Thickness(2),
            Background = new SolidColorBrush(Colors.Black, 0.2),
            Width = sel.Width,
            Height = sel.Height
        };
        Canvas.SetLeft(_sel, sel.X);
        Canvas.SetTop(_sel, sel.Y);
        _canvas.Children.Add(_sel);

        // Input hooks
        _canvas.PointerPressed += OnPress;
        _canvas.PointerMoved += OnMove;
        _canvas.PointerReleased += OnRelease;

        // Key handling on BOTH the canvas and the window
        _canvas.KeyDown += OnKey;
        this.KeyDown += OnKey;
        Opened += (_, __) => { Activate(); Focus(); _canvas.Focus(); };
    }

    private double Handle => 8 * (this.VisualRoot as TopLevel)?.RenderScaling ?? 1.0;
    private double MinSize => 16 * (this.VisualRoot as TopLevel)?.RenderScaling ?? 1.0;

    private enum Hit { None, Move, Left, Right, Top, Bottom, TL, TR, BL, BR }

    private Hit HitTest(Point p)
    {
        double L = Canvas.GetLeft(_sel), T = Canvas.GetTop(_sel);
        double R = L + _sel.Width, B = T + _sel.Height;
        bool l = Math.Abs(p.X - L) <= Handle, r = Math.Abs(p.X - R) <= Handle;
        bool t = Math.Abs(p.Y - T) <= Handle, b = Math.Abs(p.Y - B) <= Handle;

        if (l && t) return Hit.TL; if (r && t) return Hit.TR;
        if (l && b) return Hit.BL; if (r && b) return Hit.BR;
        if (l) return Hit.Left; if (r) return Hit.Right;
        if (t) return Hit.Top; if (b) return Hit.Bottom;

        var rect = new Rect(L, T, _sel.Width, _sel.Height);
        return rect.Contains(p) ? Hit.Move : Hit.None;
    }

    private void UpdateCursor(Hit h)
    {
        var c = h switch
        {
            Hit.Left or Hit.Right => new Cursor(StandardCursorType.SizeWestEast),
            Hit.Top or Hit.Bottom => new Cursor(StandardCursorType.SizeNorthSouth),
            Hit.Move => new Cursor(StandardCursorType.SizeAll),
            Hit.TL or Hit.TR or Hit.BL or Hit.BR => new Cursor(StandardCursorType.SizeAll),
            _ => Cursor.Default
        };
        _canvas.Cursor = c;
    }

    private void OnPress(object? s, PointerPressedEventArgs e)
    {
        _start = e.GetPosition(_canvas);
        _hit = HitTest(_start);
        UpdateCursor(_hit);
        _startRect = new Rect(Canvas.GetLeft(_sel), Canvas.GetTop(_sel), _sel.Width, _sel.Height);
        _canvas.Focus();
        e.Pointer.Capture(_canvas);
        e.Handled = true;
    }

    private void OnMove(object? s, PointerEventArgs e)
    {
        if (!ReferenceEquals(e.Pointer.Captured, _canvas))
        {
            UpdateCursor(HitTest(e.GetPosition(_canvas)));
            return;
        }

        var p = e.GetPosition(_canvas);
        var dx = p.X - _start.X;
        var dy = p.Y - _start.Y;
        var L = _startRect.X;
        var T = _startRect.Y;
        var W = _startRect.Width;
        var H = _startRect.Height;

        switch (_hit)
        {
            case Hit.Move: L += dx; T += dy; break;
            case Hit.Left: L += dx; W -= dx; break;
            case Hit.Right: W += dx; break;
            case Hit.Top: T += dy; H -= dy; break;
            case Hit.Bottom: H += dy; break;
            case Hit.TL: L += dx; W -= dx; T += dy; H -= dy; break;
            case Hit.TR: W += dx; T += dy; H -= dy; break;
            case Hit.BL: L += dx; W -= dx; H += dy; break;
            case Hit.BR: W += dx; H += dy; break;
        }

        L = Math.Clamp(L, 0, _canvas.Bounds.Width - MinSize);
        T = Math.Clamp(T, 0, _canvas.Bounds.Height - MinSize);
        W = Math.Clamp(W, MinSize, _canvas.Bounds.Width - L);
        H = Math.Clamp(H, MinSize, _canvas.Bounds.Height - T);

        _sel.Width = W; _sel.Height = H;
        Canvas.SetLeft(_sel, L); Canvas.SetTop(_sel, T);
        e.Handled = true;
    }

    private void OnRelease(object? s, PointerReleasedEventArgs e)
    {
        e.Pointer.Capture(null);
        _hit = Hit.None;
        UpdateCursor(_hit);
        e.Handled = true;
    }

    private void OnKey(object? sender, KeyEventArgs e)
    {
        if (e.Key == Key.Escape) { Close(); return; }
        if (e.Key == Key.Enter) { SaveAndClose(); Close(); return; }

        double step = e.KeyModifiers.HasFlag(KeyModifiers.Shift) ? 10 : 1;
        double L = Canvas.GetLeft(_sel), T = Canvas.GetTop(_sel);

        if (e.Key == Key.Left) L -= step;
        if (e.Key == Key.Right) L += step;
        if (e.Key == Key.Up) T -= step;
        if (e.Key == Key.Down) T += step;

        L = Math.Clamp(L, 0, _canvas.Bounds.Width - _sel.Width - 1);
        T = Math.Clamp(T, 0, _canvas.Bounds.Height - _sel.Height - 1);
        Canvas.SetLeft(_sel, L);
        Canvas.SetTop(_sel, T);
        e.Handled = true;
    }

    private void SaveAndClose()
    {
        var L = Canvas.GetLeft(_sel) / Width;
        var T = Canvas.GetTop(_sel) / Height;
        var W = _sel.Width / Width;
        var H = _sel.Height / Height;
        _store.Save(new Roi(L, T, W, H));
    }
}