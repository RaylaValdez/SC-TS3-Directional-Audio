using System;
using System.IO;
using System.Text.Json;
using Avalonia;
using Avalonia.Controls;
using Avalonia.Input;
using Avalonia.Markup.Xaml;

namespace RegionSelector.Views
{
    public sealed partial class MainWindow : Window
    {
        private readonly Rect _windowRect; // Star Citizen window rect
        private readonly string _outPath;

        private Canvas _root = default!;
        private Border _selection = default!;

        private Point _dragStart;
        private Rect _startRect;
        private Hit _hit = Hit.None;
        private const double HANDLE = 8;

        public MainWindow(int x, int y, int w, int h, string outPath)
        {
            InitializeComponent(); // load XAML

            _windowRect = new Rect(x, y, w, h);
            _outPath = outPath;

            Position = new PixelPoint(x, y);
            Width = w; Height = h;

            _root = this.FindControl<Canvas>("RootCanvas")!;
            _selection = this.FindControl<Border>("Selection")!;

            // Default selection (matches your current guess)
            double selW = w * 0.50, selH = h * 0.015;
            Canvas.SetLeft(_selection, w - selW);
            Canvas.SetTop(_selection, h * 0.007);
            _selection.Width = selW;
            _selection.Height = selH;

            _root.PointerPressed += OnPressed;
            _root.PointerMoved += OnMoved;
            _root.PointerReleased += OnReleased;
            KeyDown += OnKeyDown;
        }

        private void InitializeComponent() => AvaloniaXamlLoader.Load(this);

        private enum Hit { None, Move, Left, Right, Top, Bottom, TL, TR, BL, BR }

        private Hit HitTest(Point p)
        {
            double L = Canvas.GetLeft(_selection), T = Canvas.GetTop(_selection);
            double R = L + _selection.Width, B = T + _selection.Height;
            bool l = Math.Abs(p.X - L) <= HANDLE, r = Math.Abs(p.X - R) <= HANDLE;
            bool t = Math.Abs(p.Y - T) <= HANDLE, b = Math.Abs(p.Y - B) <= HANDLE;

            if (l && t) return Hit.TL;
            if (r && t) return Hit.TR;
            if (l && b) return Hit.BL;
            if (r && b) return Hit.BR;
            if (l) return Hit.Left;
            if (r) return Hit.Right;
            if (t) return Hit.Top;
            if (b) return Hit.Bottom;

            var rect = new Rect(L, T, _selection.Width, _selection.Height);
            return rect.Contains(p) ? Hit.Move : Hit.None;
        }

        private void OnPressed(object? s, PointerPressedEventArgs e)
        {
            var p = e.GetPosition(_root);
            _hit = HitTest(p);
            if (_hit == Hit.None) return;

            _dragStart = p;
            _startRect = new Rect(Canvas.GetLeft(_selection), Canvas.GetTop(_selection), _selection.Width, _selection.Height);
            e.Pointer.Capture(_root);
        }

        private void OnMoved(object? s, PointerEventArgs e)
        {
            if (!ReferenceEquals(e.Pointer.Captured, _root)) return;

            var p = e.GetPosition(_root);
            double dx = p.X - _dragStart.X;
            double dy = p.Y - _dragStart.Y;

            double L = _startRect.X, T = _startRect.Y, W = _startRect.Width, H = _startRect.Height;

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

            // Clamp to window bounds
            L = Math.Clamp(L, 0, _root.Bounds.Width - 1);
            T = Math.Clamp(T, 0, _root.Bounds.Height - 1);
            W = Math.Max(4, Math.Min(W, _root.Bounds.Width - L));
            H = Math.Max(4, Math.Min(H, _root.Bounds.Height - T));

            Canvas.SetLeft(_selection, L);
            Canvas.SetTop(_selection, T);
            _selection.Width = W;
            _selection.Height = H;
        }

        private void OnReleased(object? s, PointerReleasedEventArgs e)
        {
            if (ReferenceEquals(e.Pointer.Captured, _root))
                e.Pointer.Capture(null);
            _hit = Hit.None;
        }

        private void OnKeyDown(object? s, KeyEventArgs e)
        {
            if (e.Key == Key.Enter) SaveAndClose();
            else if (e.Key == Key.Escape) Close();
        }

        private void SaveAndClose()
        {
            double L = Canvas.GetLeft(_selection);
            double T = Canvas.GetTop(_selection);
            double W = _selection.Width;
            double H = _selection.Height;

            var roi = new RoiFractions
            {
                Left = L / _windowRect.Width,
                Top = T / _windowRect.Height,
                Width = W / _windowRect.Width,
                Height = H / _windowRect.Height
            };

            Directory.CreateDirectory(Path.GetDirectoryName(_outPath)!);
            File.WriteAllText(_outPath, JsonSerializer.Serialize(roi, new JsonSerializerOptions { WriteIndented = true }));
            Close();
        }

        private sealed class RoiFractions
        {
            public double Left { get; set; }
            public double Top { get; set; }
            public double Width { get; set; }
            public double Height { get; set; }
        }
    }
}
