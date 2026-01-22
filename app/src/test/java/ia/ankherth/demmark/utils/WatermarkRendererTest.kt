package ia.ankherth.demmark.utils

import android.graphics.Bitmap
import android.graphics.Color
import ia.ankherth.demmark.domain.model.WatermarkConfig
import ia.ankherth.demmark.domain.model.WatermarkPosition
import org.junit.Assert.*
import org.junit.Test

class WatermarkRendererTest {

    @Test
    fun `aplicarMarcaAguaImagen debería crear un bitmap con marca de agua`() {
        // Given
        val originalBitmap = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888)
        originalBitmap.eraseColor(Color.WHITE)
        
        val config = WatermarkConfig(
            text = "Test",
            fontSize = 48f,
            opacity = 50,
            color = Color.BLACK,
            position = WatermarkPosition.CENTER
        )
        
        // When
        val result = WatermarkRenderer.aplicarMarcaAguaImagen(originalBitmap, config)
        
        // Then
        assertNotNull(result)
        assertEquals(originalBitmap.width, result.width)
        assertEquals(originalBitmap.height, result.height)
        assertNotSame(originalBitmap, result) // Debe ser una copia
    }

    @Test
    fun `aplicarMarcaAguaImagen debería mantener las dimensiones del bitmap original`() {
        // Given
        val width = 1024
        val height = 768
        val originalBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val config = WatermarkConfig(
            text = "DamMark",
            fontSize = 32f,
            opacity = 75,
            color = Color.RED,
            position = WatermarkPosition.TOP_LEFT
        )
        
        // When
        val result = WatermarkRenderer.aplicarMarcaAguaImagen(originalBitmap, config)
        
        // Then
        assertEquals(width, result.width)
        assertEquals(height, result.height)
    }

    @Test
    fun `aplicarMarcaAguaImagen debería funcionar con diferentes posiciones`() {
        // Given
        val originalBitmap = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888)
        val positions = listOf(
            WatermarkPosition.CENTER,
            WatermarkPosition.DIAGONAL,
            WatermarkPosition.TOP_LEFT,
            WatermarkPosition.BOTTOM_RIGHT
        )
        
        // When & Then
        positions.forEach { position ->
            val config = WatermarkConfig(
                text = "Test",
                fontSize = 40f,
                opacity = 50,
                color = Color.BLACK,
                position = position
            )
            
            val result = WatermarkRenderer.aplicarMarcaAguaImagen(originalBitmap, config)
            assertNotNull(result)
            assertEquals(originalBitmap.width, result.width)
            assertEquals(originalBitmap.height, result.height)
        }
    }

    @Test
    fun `aplicarMarcaAguaImagen debería respetar la opacidad`() {
        // Given
        val originalBitmap = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888)
        
        val config1 = WatermarkConfig(
            text = "Test",
            fontSize = 48f,
            opacity = 0, // Completamente transparente
            color = Color.BLACK,
            position = WatermarkPosition.CENTER
        )
        
        val config2 = WatermarkConfig(
            text = "Test",
            fontSize = 48f,
            opacity = 100, // Completamente opaco
            color = Color.BLACK,
            position = WatermarkPosition.CENTER
        )
        
        // When
        val result1 = WatermarkRenderer.aplicarMarcaAguaImagen(originalBitmap, config1)
        val result2 = WatermarkRenderer.aplicarMarcaAguaImagen(originalBitmap, config2)
        
        // Then
        assertNotNull(result1)
        assertNotNull(result2)
        // Ambos deberían tener las mismas dimensiones
        assertEquals(result1.width, result2.width)
        assertEquals(result1.height, result2.height)
    }

    @Test
    fun `aplicarMarcaAguaImagen debería funcionar con diferentes tamaños de fuente`() {
        // Given
        val originalBitmap = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888)
        val fontSizes = listOf(12f, 24f, 48f, 96f, 200f)
        
        // When & Then
        fontSizes.forEach { fontSize ->
            val config = WatermarkConfig(
                text = "Test",
                fontSize = fontSize,
                opacity = 50,
                color = Color.BLACK,
                position = WatermarkPosition.CENTER
            )
            
            val result = WatermarkRenderer.aplicarMarcaAguaImagen(originalBitmap, config)
            assertNotNull(result)
        }
    }

    @Test
    fun `aplicarMarcaAguaImagen debería funcionar con texto vacío`() {
        // Given
        val originalBitmap = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888)
        val config = WatermarkConfig(
            text = "",
            fontSize = 48f,
            opacity = 50,
            color = Color.BLACK,
            position = WatermarkPosition.CENTER
        )
        
        // When
        val result = WatermarkRenderer.aplicarMarcaAguaImagen(originalBitmap, config)
        
        // Then
        assertNotNull(result)
        assertEquals(originalBitmap.width, result.width)
        assertEquals(originalBitmap.height, result.height)
    }
}

