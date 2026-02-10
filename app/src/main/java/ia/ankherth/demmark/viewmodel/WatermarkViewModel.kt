package ia.ankherth.demmark.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ia.ankherth.demmark.data.FileType
import ia.ankherth.demmark.data.WatermarkState
import ia.ankherth.demmark.util.FileUtil
import ia.ankherth.demmark.util.PdfWatermarkUtil
import ia.ankherth.demmark.util.ValidationUtil
import ia.ankherth.demmark.util.WatermarkUtil
import ia.ankherth.demmark.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WatermarkViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _state = MutableLiveData<WatermarkState>(WatermarkState())
    val state: LiveData<WatermarkState> = _state
    
    private val _preview = MutableLiveData<Bitmap?>()
    val preview: LiveData<Bitmap?> = _preview
    
    private val _statusUpdate = MutableLiveData<String>()
    val statusUpdate: LiveData<String> = _statusUpdate
    
    fun setFileUri(uri: Uri, fileType: FileType) {
        updateState { copy(fileUri = uri, fileType = fileType) }
        loadPreview(uri, fileType)
    }
    
    fun setWatermarkText(text: String) {
        updateState { copy(watermarkText = text) }
    }

    fun setOpacity(opacity: Int) {
        updateState { copy(opacity = opacity.coerceIn(1, 254)) }
    }
    
    fun applyWatermark() {
        val currentState = _state.value ?: return
        
        if (!ValidationUtil.isValidUri(currentState.fileUri)) {
            _statusUpdate.postValue("Please select a file")
            return
        }
        
        if (!ValidationUtil.isValidWatermarkText(currentState.watermarkText)) {
            _statusUpdate.postValue("Watermark text must be between 1 and 100 characters")
            return
        }
        
        if (!ValidationUtil.canReadFile(getApplication(), currentState.fileUri!!)) {
            _statusUpdate.postValue("Cannot read the selected file")
            return
        }
        
        // Apply watermark in memory only. Rendering off UI thread.
        updateState { copy(isProcessing = true, statusMessage = "Processing...") }

        viewModelScope.launch {
            try {
                when (currentState.fileType) {
                    FileType.IMAGE -> processImageInMemory(currentState.fileUri, currentState.watermarkText)
                    FileType.PDF -> _statusUpdate.postValue("PDF not supported for in-memory apply")
                    null -> _statusUpdate.postValue("Invalid file type")
                }
            } catch (e: Exception) {
                _statusUpdate.postValue("Error: ${e.message}")
                updateState { copy(isProcessing = false) }
            }
        }
    }
    
    private suspend fun processImageInMemory(uri: Uri?, watermarkText: String) {
        withContext(Dispatchers.Default) {
            if (uri == null) {
                _statusUpdate.postValue("Invalid image uri")
                updateState { copy(isProcessing = false) }
                return@withContext
            }

            val bitmap = FileUtil.loadBitmapFromUri(getApplication(), uri)
            if (bitmap == null) {
                _statusUpdate.postValue("Failed to load image")
                updateState { copy(isProcessing = false) }
                return@withContext
            }

            // Convert configured text size (SP) to pixels using display metrics
            val metrics = getApplication<Application>().resources.displayMetrics
            val textSizeSp = Constants.WATERMARK_TEXT_SIZE
            @Suppress("DEPRECATION")
            val textSizePx = textSizeSp * metrics.scaledDensity
            val opacity = _state.value?.opacity ?: Constants.WATERMARK_OPACITY

            val result = WatermarkUtil.addWatermarkToImage(
                bitmap,
                watermarkText,
                textSizePx,
                opacity,
                Constants.WATERMARK_SPACING
            )

            withContext(Dispatchers.Main) {
                _preview.postValue(result)
                _statusUpdate.postValue("Preview updated")
                updateState { copy(isProcessing = false, statusMessage = "Complete") }
            }
        }
    }
    
    private suspend fun processPdf(uri: Uri, watermarkText: String) {
        withContext(Dispatchers.IO) {
            val savedFile = PdfWatermarkUtil.addWatermarkToPdf(getApplication(), uri, watermarkText)
            
            withContext(Dispatchers.Main) {
                if (savedFile != null) {
                    _statusUpdate.postValue("PDF watermarked: ${savedFile.absolutePath}")
                    updateState { copy(isProcessing = false, statusMessage = "Complete") }
                } else {
                    _statusUpdate.postValue("Failed to watermark PDF")
                    updateState { copy(isProcessing = false) }
                }
            }
        }
    }
    
    private fun loadPreview(uri: Uri, fileType: FileType) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (fileType == FileType.IMAGE) {
                    val bitmap = FileUtil.loadBitmapFromUri(getApplication(), uri)
                    withContext(Dispatchers.Main) {
                        _preview.postValue(bitmap)
                    }
                } else if (fileType == FileType.PDF) {
                    val bitmap = FileUtil.extractPdfFirstPage(getApplication(), uri)
                    withContext(Dispatchers.Main) {
                        _preview.postValue(bitmap)
                    }
                }
            }
        }
    }

    /**
     * Save the currently previewed bitmap to external MediaStore. This function
     * never applies or re-applies any watermark; it only saves whatever is in `_preview`.
     */
    fun savePreviewToGallery() {
        val bmp = _preview.value ?: run {
            _statusUpdate.postValue("No processed image to save")
            return
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val filename = "demmark_${System.currentTimeMillis()}.png"
                    val saved = FileUtil.saveBitmapToMediaStore(getApplication(), bmp, filename)
                    withContext(Dispatchers.Main) {
                        if (saved) {
                            _statusUpdate.postValue("Guardar imagen: OK")
                        } else {
                            _statusUpdate.postValue("Failed to save image")
                        }
                    }
                } catch (e: Exception) {
                    _statusUpdate.postValue("Error saving image: ${e.message}")
                }
            }
        }
    }
    
    private fun updateState(block: WatermarkState.() -> WatermarkState) {
        val currentState = _state.value ?: WatermarkState()
        _state.postValue(currentState.block())
    }
}
