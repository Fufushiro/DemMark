package ia.ankherth.demmark.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ia.ankherth.demmark.data.repository.FileRepository
import ia.ankherth.demmark.domain.model.FileType
import ia.ankherth.demmark.domain.model.WatermarkConfig
import ia.ankherth.demmark.domain.model.WatermarkPosition
import ia.ankherth.demmark.utils.DocumentRenderer
import ia.ankherth.demmark.utils.ImageSaver
import ia.ankherth.demmark.utils.WatermarkRenderer
import kotlinx.coroutines.launch

class WatermarkEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val fileRepository = FileRepository(application)
    private val contentResolver = application.contentResolver

    private val _watermarkConfig = MutableLiveData<WatermarkConfig>(WatermarkConfig())
    val watermarkConfig: LiveData<WatermarkConfig> = _watermarkConfig

    private val _originalBitmap = MutableLiveData<Bitmap?>()
    val originalBitmap: LiveData<Bitmap?> = _originalBitmap

    private val _previewBitmap = MutableLiveData<Bitmap?>()
    val previewBitmap: LiveData<Bitmap?> = _previewBitmap

    private val _fileType = MutableLiveData<FileType?>()
    val fileType: LiveData<FileType?> = _fileType

    private val _fileUri = MutableLiveData<Uri?>()
    val fileUri: LiveData<Uri?> = _fileUri

    private val _isSaving = MutableLiveData<Boolean>(false)
    val isSaving: LiveData<Boolean> = _isSaving

    private val _saveResult = MutableLiveData<Result<Uri>?>()
    val saveResult: LiveData<Result<Uri>?> = _saveResult

    fun initialize(uri: Uri, fileType: FileType) {
        if (_fileUri.value == uri && _fileType.value == fileType && _originalBitmap.value != null) {
            return
        }

        _fileUri.value = uri
        _fileType.value = fileType

        viewModelScope.launch {
            DocumentRenderer.renderDocument(contentResolver, uri, fileType).fold(
                onSuccess = { bitmap ->
                    _originalBitmap.value = bitmap
                    updatePreview()
                },
                onFailure = { error ->
                    _saveResult.value = Result.failure(error)
                }
            )
        }
    }

    fun updateText(text: String) {
        val current = _watermarkConfig.value ?: WatermarkConfig()
        _watermarkConfig.value = current.copy(text = text)
        updatePreview()
    }

    fun updateFontSize(size: Float) {
        val current = _watermarkConfig.value ?: WatermarkConfig()
        _watermarkConfig.value = current.copy(fontSize = size)
        updatePreview()
    }

    fun updateOpacity(opacity: Int) {
        val current = _watermarkConfig.value ?: WatermarkConfig()
        _watermarkConfig.value = current.copy(opacity = opacity.coerceIn(0, 100))
        updatePreview()
    }

    fun updateColor(color: Int) {
        val current = _watermarkConfig.value ?: WatermarkConfig()
        _watermarkConfig.value = current.copy(color = color)
        updatePreview()
    }

    fun updatePosition(position: WatermarkPosition) {
        val current = _watermarkConfig.value ?: WatermarkConfig()
        _watermarkConfig.value = current.copy(position = position)
        updatePreview()
    }

    private fun updatePreview() {
        val original = _originalBitmap.value
        val config = _watermarkConfig.value

        if (original != null && config != null) {
            viewModelScope.launch {
                val preview = WatermarkRenderer.aplicarMarcaAguaImagen(original, config)
                _previewBitmap.value = preview
            }
        }
    }

    fun saveFile(format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG) {
        val config = _watermarkConfig.value ?: return
        val fileType = _fileType.value ?: return
        val uri = _fileUri.value ?: return
        val preview = _previewBitmap.value ?: return

        if (!fileRepository.hasSaveFolder()) {
            _saveResult.value = Result.failure(Exception("Carpeta de guardado no configurada"))
            return
        }

        _isSaving.value = true

        viewModelScope.launch {
            try {
                val fileName = when (fileType) {
                    FileType.IMAGE -> fileRepository.generateFileName("image", if (format == Bitmap.CompressFormat.JPEG) "jpg" else "png")
                    FileType.PDF -> fileRepository.generateFileName("document", if (format == Bitmap.CompressFormat.JPEG) "jpg" else "png")
                }

                val saveUri = fileRepository.getSaveFolderUri()
                val result = ImageSaver.saveBitmapAsImage(
                    getApplication(),
                    contentResolver,
                    preview,
                    fileName,
                    format,
                    saveUri
                )

                _saveResult.value = result
            } catch (e: Exception) {
                _saveResult.value = Result.failure(e)
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun clearSaveResult() {
        _saveResult.value = null
    }
}

