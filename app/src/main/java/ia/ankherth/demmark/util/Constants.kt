package ia.ankherth.demmark.util

object Constants {
    // Default opacity (0-255). User controls this; defaults low but visible.
    const val WATERMARK_OPACITY = 48
    // Default text size in SP (will be converted to pixels by renderer using display metrics).
    const val WATERMARK_TEXT_SIZE = 10f
    // Spacing in pixels between repetitions on screen (small, constant)
    const val WATERMARK_SPACING = 36
    const val PDF_RENDER_DPI = 150f
}
