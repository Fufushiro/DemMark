package ia.ankherth.demmark.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import ia.ankherth.demmark.domain.model.FileType

class FileRepository(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "dammark_prefs"
        private const val KEY_SAVE_FOLDER_URI = "save_folder_uri"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSaveFolderUri(): Uri? {
        val uriString = prefs.getString(KEY_SAVE_FOLDER_URI, null)
        return uriString?.let { Uri.parse(it) }
    }

    fun setSaveFolderUri(uri: Uri) {
        prefs.edit().putString(KEY_SAVE_FOLDER_URI, uri.toString()).apply()
        context.contentResolver.takePersistableUriPermission(
            uri,
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }

    fun hasSaveFolder(): Boolean = getSaveFolderUri() != null

    fun getFileTypeFromUri(uri: Uri): FileType? {
        val mimeType = context.contentResolver.getType(uri)
        return when {
            mimeType?.startsWith("image/") == true -> FileType.IMAGE
            mimeType == "application/pdf" -> FileType.PDF
            else -> null
        }
    }

    fun generateFileName(prefix: String, extension: String): String {
        val timestamp = System.currentTimeMillis()
        return "${prefix}_watermarked_$timestamp.$extension"
    }
}

