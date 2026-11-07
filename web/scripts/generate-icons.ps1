# Render the existing BrandMark scooter as PNGs for browser installation.
Add-Type -AssemblyName System.Drawing
$iconDirectory = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../public/icons'))
New-Item -ItemType Directory -Force -Path $iconDirectory | Out-Null
foreach ($size in @(96, 180, 192, 512)) {
    $bitmap = [System.Drawing.Bitmap]::new($size, $size)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.Clear($(if ($size -eq 96) { [System.Drawing.Color]::Transparent } else { [System.Drawing.ColorTranslator]::FromHtml('#ff5a00') }))
    $graphics.ScaleTransform($size / 64.0, $size / 64.0)
    # Keep the scooter inside the central mask-safe area for Android and iOS.
    $graphics.TranslateTransform(8, 14)
    $graphics.ScaleTransform(0.75, 0.75)
    $pen = [System.Drawing.Pen]::new([System.Drawing.Color]::White, 3.5)
    $pen.StartCap = $pen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
    $pen.LineJoin = [System.Drawing.Drawing2D.LineJoin]::Round
    $graphics.DrawEllipse($pen, 6, 28, 16, 16)
    $graphics.DrawEllipse($pen, 43, 28, 16, 16)
    $path = [System.Drawing.Drawing2D.GraphicsPath]::new()
    $path.AddLine(14, 36, 35, 36)
    $path.AddBezier(35, 36, 41, 36, 44, 32, 44, 27)
    $path.AddLine(44, 27, 38, 9)
    $path.AddLine(38, 9, 30, 9)
    $path.StartFigure(); $path.AddLine(35, 9, 43, 9)
    $path.StartFigure(); $path.AddLine(36, 17, 22, 17); $path.AddLine(22, 17, 15, 28); $path.AddLine(15, 28, 36, 28)
    $path.StartFigure(); $path.AddLine(7, 17, 24, 17); $path.AddLine(24, 17, 24, 26); $path.AddLine(24, 26, 7, 26); $path.CloseFigure()
    $path.StartFigure(); $path.AddLine(5, 12, 22, 12)
    $path.StartFigure(); $path.AddLine(49, 28, 44, 15)
    $path.StartFigure(); $path.AddLine(11, 32, 15, 25)
    $path.StartFigure(); $path.AddLine(31, 5, 38, 5)
    $graphics.DrawPath($pen, $path)
    $name = if ($size -eq 96) { 'kapinda-badge-96.png' } else { "kapinda-$size.png" }
    $bitmap.Save((Join-Path $iconDirectory $name), [System.Drawing.Imaging.ImageFormat]::Png)
    # Also refresh the original paths for browsers with an older cached manifest.
    if ($size -eq 192 -or $size -eq 512) {
        $bitmap.Save((Join-Path $iconDirectory "icon-$size.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    }
    $path.Dispose(); $pen.Dispose(); $graphics.Dispose(); $bitmap.Dispose()
}
