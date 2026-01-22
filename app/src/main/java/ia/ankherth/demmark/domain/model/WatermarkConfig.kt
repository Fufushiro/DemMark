package ia.ankherth.demmark.domain.model

/**
 * Configuración de la marca de agua
 */
data class WatermarkConfig(
    val text: String = "DamMark",
    val fontSize: Float = 48f,
    val opacity: Int = 50, // 0-100
    val color: Int = android.graphics.Color.BLACK,
    val position: WatermarkPosition = WatermarkPosition.CENTER
)

/**
 * Posiciones disponibles para la marca de agua
 */
enum class WatermarkPosition {
    CENTER,
    DIAGONAL,
    TOP_LEFT,
    BOTTOM_RIGHT
}

