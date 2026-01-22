package ia.ankherth.demmark.utils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import ia.ankherth.demmark.domain.model.FileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object DocumentRenderer {

    suspend fun renderDocument(
        contentResolver: ContentResolver,
        uri: Uri,
        fileType: FileType
    ): Result<Bitmap> = withContext(Dispatchers.Default) {
        try {
            when (fileType) {
                FileType.IMAGE -> renderImage(contentResolver, uri)
                FileType.PDF -> renderPdfFirstPage(contentResolver, uri)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun renderImage(
        contentResolver: ContentResolver,
        uri: Uri
    ): Result<Bitmap> = withContext(Dispatchers.IO) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure<Bitmap>(Exception("No se pudo abrir el stream"))

            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            val maxDimension = 2048
            val scale = calculateInSampleSize(options, maxDimension, maxDimension)

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = scale
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val newStream = contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure<Bitmap>(Exception("No se pudo abrir el stream"))

            val bitmap = BitmapFactory.decodeStream(newStream, null, decodeOptions)
            newStream.close()

            if (bitmap != null) {
                val immutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)
                Result.success(immutableBitmap)
            } else {
                Result.failure(Exception("No se pudo decodificar la imagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun renderPdfFirstPage(
        contentResolver: ContentResolver,
        uri: Uri
    ): Result<Bitmap> = withContext(Dispatchers.IO) {
        var inputPfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        var page: PdfRenderer.Page? = null

        try {
            inputPfd = contentResolver.openFileDescriptor(uri, "r")
                ?: return@withContext Result.failure<Bitmap>(Exception("No se pudo abrir el PDF"))

            renderer = PdfRenderer(inputPfd)

            if (renderer.pageCount == 0) {
                return@withContext Result.failure<Bitmap>(Exception("PDF vacío"))
            }

            page = renderer.openPage(0)
            val bitmap = Bitmap.createBitmap(
                page.width,
                page.height,
                Bitmap.Config.ARGB_8888
            )

            bitmap.eraseColor(android.graphics.Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            page.close()
            page = null
            renderer.close()
            renderer = null
            inputPfd.close()
            inputPfd = null

            val immutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)
            Result.success(immutableBitmap)
        } catch (e: Exception) {
            page?.close()
            renderer?.close()
            inputPfd?.close()
            Result.failure(e)
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight &&
                (halfWidth / inSampleSize) >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
}

