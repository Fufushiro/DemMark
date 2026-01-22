package ia.ankherth.demmark.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageSaver {

    suspend fun saveBitmapAsImage(
        context: Context,
        contentResolver: ContentResolver,
        bitmap: Bitmap,
        fileName: String,
        format: Bitmap.CompressFormat,
        folderUri: Uri?
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val mimeType = when (format) {
                Bitmap.CompressFormat.JPEG -> "image/jpeg"
                Bitmap.CompressFormat.PNG -> "image/png"
                else -> "image/png"
            }

            if (folderUri == null) {
                return@withContext Result.failure(Exception("Carpeta de guardado no configurada"))
            }

            val rootDir = DocumentFile.fromTreeUri(context, folderUri)
                ?: return@withContext Result.failure(Exception("No se pudo acceder a la carpeta"))

            val imageFile = rootDir.createFile(mimeType, fileName)
                ?: return@withContext Result.failure(Exception("No se pudo crear el archivo"))

            contentResolver.openOutputStream(imageFile.uri)?.use { output ->
                val quality = if (format == Bitmap.CompressFormat.JPEG) 90 else 100
                val success = bitmap.compress(format, quality, output)
                if (success) {
                    Result.success(imageFile.uri)
                } else {
                    Result.failure(Exception("Error al comprimir la imagen"))
                }
            } ?: Result.failure(Exception("No se pudo abrir el stream de salida"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

