package ia.ankherth.demmark.util

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File

object FileUtil {
    
    fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val contentResolver = context.contentResolver
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    fun extractPdfFirstPage(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmapBuffer = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            bitmapBuffer
        } catch (e: Exception) {
            null
        }
    }
    
    fun saveBitmapToFile(context: Context, bitmap: Bitmap, filename: String): File? {
        return try {
            val cacheDir = context.cacheDir
            val file = File(cacheDir, filename)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, file.outputStream())
            file
        } catch (e: Exception) {
            null
        }
    }

    fun saveBitmapToMediaStore(context: Context, bitmap: Bitmap, filename: String): Boolean {
        return try {
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/DemMark")
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(android.provider.MediaStore.Images.Media.getContentUri(android.provider.MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues)
                ?: return false

            resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun getFileNameFromUri(context: Context, uri: Uri): String {
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                it.getString(it.getColumnIndexOrThrow("_display_name"))
            } else {
                "file_${System.currentTimeMillis()}"
            }
        } ?: "file_${System.currentTimeMillis()}"
    }
}
