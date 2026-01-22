package ia.ankherth.demmark.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ia.ankherth.demmark.data.repository.FileRepository
import ia.ankherth.demmark.domain.model.FileType

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val fileRepository = FileRepository(application)

    private val _selectedFileUri = MutableLiveData<Uri?>()
    val selectedFileUri: LiveData<Uri?> = _selectedFileUri

    private val _fileType = MutableLiveData<FileType?>()
    val fileType: LiveData<FileType?> = _fileType

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _needsSaveFolder = MutableLiveData<Boolean>()
    val needsSaveFolder: LiveData<Boolean> = _needsSaveFolder

    init {
        checkSaveFolder()
    }

    private fun checkSaveFolder() {
        _needsSaveFolder.value = !fileRepository.hasSaveFolder()
    }

    fun selectFile(uri: Uri) {
        _selectedFileUri.value = uri
        _fileType.value = fileRepository.getFileTypeFromUri(uri)
        _error.value = null
    }

    fun setSaveFolder(uri: Uri) {
        fileRepository.setSaveFolderUri(uri)
        _needsSaveFolder.value = false
    }

    fun clearError() {
        _error.value = null
    }
}

