package ia.ankherth.demmark.util

import android.content.Context
import android.net.Uri
import android.graphics.Bitmap

object ValidationUtil {
    
    fun isValidUri(uri: Uri?): Boolean = uri != null
    
    fun isValidWatermarkText(text: String?): Boolean {
        return !text.isNullOrBlank() && text.length <= 100
    }
    
    fun isValidBitmap(bitmap: Bitmap?): Boolean {
        return bitmap != null && bitmap.width > 0 && bitmap.height > 0
    }
    
    fun canReadFile(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.close()
            true
        } catch (e: Exception) {
            false
        }
    }
}
