package ia.ankherth.demmark.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import ia.ankherth.demmark.domain.model.WatermarkConfig
import ia.ankherth.demmark.domain.model.WatermarkPosition

/**
 * Componente para aplicar marcas de agua a imágenes y PDFs
 */
object WatermarkRenderer {

    suspend fun aplicarMarcaAguaImagen(
        bitmap: Bitmap,
        config: WatermarkConfig
    ): Bitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
        val workingBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(workingBitmap)
        
        val paint = Paint().apply {
            color = config.color
            textSize = config.fontSize
            alpha = (255 * config.opacity / 100).toInt().coerceIn(0, 255)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        val textBounds = android.graphics.Rect()
        paint.getTextBounds(config.text, 0, config.text.length, textBounds)
        
        val textWidth = textBounds.width().toFloat()
        val textHeight = textBounds.height().toFloat()
        val canvasWidth = canvas.width.toFloat()
        val canvasHeight = canvas.height.toFloat()
        
        val spacingX = textWidth * 1.5f
        val spacingY = textHeight * 2.0f
        val rotation = when (config.position) {
            WatermarkPosition.DIAGONAL -> -45f
            else -> 0f
        }
        
        var startY = textHeight
        while (startY < canvasHeight + spacingY) {
            var startX = textWidth / 2f
            while (startX < canvasWidth + spacingX) {
                canvas.save()
                if (rotation != 0f) {
                    canvas.rotate(rotation, startX, startY)
                }
                canvas.drawText(config.text, startX, startY, paint)
                canvas.restore()
                startX += spacingX
            }
            startY += spacingY
        }
        
        val immutableResult = workingBitmap.copy(Bitmap.Config.ARGB_8888, false)
        immutableResult
    }

    suspend fun renderPdfPagesAsBitmaps(
        contentResolver: android.content.ContentResolver,
        pdfUri: Uri,
        config: WatermarkConfig
    ): Result<List<Bitmap>> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
        var inputPfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        val bitmaps = mutableListOf<Bitmap>()

        try {
            inputPfd = contentResolver.openFileDescriptor(pdfUri, "r")
                ?: return@withContext Result.failure(Exception("No se pudo abrir el PDF"))

            renderer = PdfRenderer(inputPfd)

            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                val pageWidth = page.width
                val pageHeight = page.height

                val bitmap = Bitmap.createBitmap(
                    pageWidth,
                    pageHeight,
                    Bitmap.Config.ARGB_8888
                )

                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                val watermarkedBitmap = aplicarMarcaAguaImagen(bitmap, config)
                bitmaps.add(watermarkedBitmap)

                page.close()
            }

            renderer.close()
            renderer = null
            inputPfd.close()
            inputPfd = null

            Result.success(bitmaps)
        } catch (e: Exception) {
            bitmaps.forEach { it.recycle() }
            renderer?.close()
            inputPfd?.close()
            Result.failure(e)
        }
    }

}

