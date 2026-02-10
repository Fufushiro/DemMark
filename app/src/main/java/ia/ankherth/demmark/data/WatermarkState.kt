package ia.ankherth.demmark.data

import android.net.Uri

data class WatermarkState(
    val fileUri: Uri? = null,
    val fileType: FileType? = null,
    val watermarkText: String = "",
    val opacity: Int = ia.ankherth.demmark.util.Constants.WATERMARK_OPACITY,
    val isProcessing: Boolean = false,
    val statusMessage: String = ""
)

enum class FileType {
    IMAGE, PDF
}
