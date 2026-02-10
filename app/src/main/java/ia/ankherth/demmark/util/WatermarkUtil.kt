package ia.ankherth.demmark.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color as AndroidColor
import kotlin.math.min

object WatermarkUtil {

    /**
     * Advanced microtext watermark renderer.
     * - Implements dense repeated literal text (no semantic changes)
     * - Fixed micro text size (px) provided by caller
     * - Deterministic micro-warp per small blocks to break perfect alignments
     * - Local, subtle contrast/gamma/alpha variations per block
     * - Low-amplitude luminance noise pass and tiny LSB/alpha tweaks
     * Notes: algorithm favors human legibility while disrupting automated cleaning.
     */
    fun addWatermarkToImage(
        bitmap: Bitmap,
        watermarkText: String,
        textSizePx: Float,
        opacity: Int,
        spacingPx: Int
    ): Bitmap {
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val result = bitmap.copy(config, true)
        val canvas = Canvas(result)

        val baseGray = 200
        val baseColor = AndroidColor.rgb(baseGray, baseGray, baseGray)

        val paint = Paint().apply {
            color = baseColor
            textSize = textSizePx
            alpha = opacity.coerceIn(1, 254)
            isAntiAlias = true
            isFilterBitmap = false
        }

        // Block grid for micro-warp (keeps local coherence)
        val blockSize = 16 // pixels; regular block grid
        val nx = (result.width + blockSize - 1) / blockSize + 1
        val ny = (result.height + blockSize - 1) / blockSize + 1

        // Deterministic PRNG per seed (watermark text + constants)
        fun seededRand(seed: Int): java.util.Random = java.util.Random(seed.toLong())

        // Generate node offsets (one extra row/col for interpolation)
        val seedBase = watermarkText.hashCode()
        val maxOffset = 1.2f // sub-pixel to small-pixel offsets
        val nodeOffsetsX = Array(ny) { FloatArray(nx) }
        val nodeOffsetsY = Array(ny) { FloatArray(nx) }
        val nodeAlphaFactor = Array(ny) { FloatArray(nx) }
        val nodeLumaFactor = Array(ny) { FloatArray(nx) }

        for (j in 0 until ny) {
            for (i in 0 until nx) {
                val rnd = seededRand(seedBase xor (i * 374761393) xor (j * 668265263))
                // small coherent offsets
                nodeOffsetsX[j][i] = (rnd.nextFloat() * 2f - 1f) * maxOffset
                nodeOffsetsY[j][i] = (rnd.nextFloat() * 2f - 1f) * maxOffset
                // subtle local alpha multiplier around 1.0 (0.92 - 1.08)
                nodeAlphaFactor[j][i] = 0.98f + (rnd.nextFloat() - 0.5f) * 0.2f
                // subtle luminance multiplier (0.96 - 1.06)
                nodeLumaFactor[j][i] = 1.0f + (rnd.nextFloat() - 0.5f) * 0.12f
            }
        }

        // Text metrics
        val fm = paint.fontMetrics
        val textHeight = (fm.descent - fm.ascent)
        val textWidth = paint.measureText(watermarkText)

        // Dense tiled repetition covering 100% of canvas
        var rowY = 0f
        val stepY = textHeight + spacingPx
        while (rowY < result.height + textHeight) {
            var colX = 0f
            while (colX < result.width + textWidth) {
                // compute block-relative position to interpolate node offsets
                // position is baseline usage: yBaseline = rowY - fm.descent
                val xPos = colX
                val yPos = rowY

                // node indices for interpolation
                val gx = (xPos / blockSize).toInt().coerceIn(0, nx - 2)
                val gy = (yPos / blockSize).toInt().coerceIn(0, ny - 2)
                val tx = (xPos % blockSize) / blockSize.toFloat()
                val ty = (yPos % blockSize) / blockSize.toFloat()

                // Bilinear interpolation of node offsets and local factors
                fun bilerp(arr: Array<FloatArray>): Float {
                    val v00 = arr[gy][gx]
                    val v10 = arr[gy][gx + 1]
                    val v01 = arr[gy + 1][gx]
                    val v11 = arr[gy + 1][gx + 1]
                    val a = v00 * (1 - tx) + v10 * tx
                    val b = v01 * (1 - tx) + v11 * tx
                    return a * (1 - ty) + b * ty
                }

                val dx = bilerp(nodeOffsetsX)
                val dy = bilerp(nodeOffsetsY)
                val localAlphaMul = bilerp(nodeAlphaFactor)
                val localLumaMul = bilerp(nodeLumaFactor)

                // prepare paint color variant per repetition
                val gray = (baseGray * localLumaMul).toInt().coerceIn(32, 240)
                val alpha = (opacity.coerceIn(1, 254) * localAlphaMul).toInt().coerceIn(1, 254)
                paint.color = AndroidColor.argb(alpha, gray, gray, gray)

                // draw text at displaced position (baseline adjusted)
                val drawX = xPos + dx
                val drawY = yPos - fm.descent + dy
                canvas.drawText(watermarkText, drawX, drawY, paint)

                // tiny additional pass to subtly break perfect edges (very low-alpha jitter)
                val jitter = 0.3f
                val extraAlpha = (alpha * 0.12f).toInt().coerceAtLeast(1)
                paint.color = AndroidColor.argb(extraAlpha, gray, gray, gray)
                canvas.drawText(watermarkText, drawX + jitter, drawY - jitter, paint)

                colX += textWidth + spacingPx
            }
            rowY += stepY
        }

        // Final low-amplitude luminance noise + tiny LSB/alpha tweaks (deterministic)
        applyLuminanceNoiseAndLSBTweaks(result, watermarkText)

        return result
    }

    private fun applyLuminanceNoiseAndLSBTweaks(bitmap: Bitmap, seedText: String) {
        val rnd = java.util.Random(seedText.hashCode().toLong() xor -4734768741142671515L)
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width)

        // noise amplitude small (luma delta in [-3,3])
        val lumaAmp = 3
        for (y in 0 until height) {
            bitmap.getPixels(pixels, 0, width, 0, y, width, 1)
            for (x in 0 until width) {
                val c = pixels[x]
                val a = AndroidColor.alpha(c)
                var r = AndroidColor.red(c)
                var g = AndroidColor.green(c)
                var b = AndroidColor.blue(c)

                // convert to luminance, perturb, convert back preserving chroma roughly
                val yL = (0.299f * r + 0.587f * g + 0.114f * b)
                val delta = (rnd.nextInt(lumaAmp * 2 + 1) - lumaAmp).toFloat()
                val newY = (yL + delta).coerceIn(0f, 255f)

                // scale chroma to match new luminance proportionally
                val scale = if (yL > 1f) newY / yL else 1f
                r = (r * scale).toInt().coerceIn(0, 255)
                g = (g * scale).toInt().coerceIn(0, 255)
                b = (b * scale).toInt().coerceIn(0, 255)

                // extremely low probability LSB tweak on alpha channel to help persistence
                if (rnd.nextInt(1000) == 0) {
                    // flip lowest bit of alpha or add 1 safely
                    val newA = if ((a and 1) == 0) (a or 1) else (a and 0xFE)
                    pixels[x] = AndroidColor.argb(newA, r, g, b)
                } else {
                    pixels[x] = AndroidColor.argb(a, r, g, b)
                }
            }
            bitmap.setPixels(pixels, 0, width, 0, y, width, 1)
        }
    }
}
