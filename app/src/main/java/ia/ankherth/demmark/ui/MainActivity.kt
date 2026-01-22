package ia.ankherth.demmark.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ia.ankherth.demmark.databinding.ActivityMainBinding
import ia.ankherth.demmark.domain.model.FileType
import ia.ankherth.demmark.ui.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.selectFile(it)
        }
    }

    private val folderPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.setSaveFolder(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.needsSaveFolder.observe(this) { needs ->
            if (needs == true) {
                folderPickerLauncher.launch(null)
            }
        }

        viewModel.selectedFileUri.observe(this) { uri ->
            uri?.let {
                val fileType = viewModel.fileType.value
                if (fileType != null) {
                    openWatermarkEditor(it, fileType)
                }
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupClickListeners() {
        binding.selectFileButton.setOnClickListener {
            filePickerLauncher.launch("*/*")
        }
    }

    private fun openWatermarkEditor(uri: Uri, fileType: FileType) {
        val intent = Intent(this, WatermarkEditorActivity::class.java).apply {
            putExtra(WatermarkEditorActivity.EXTRA_FILE_URI, uri.toString())
            putExtra(WatermarkEditorActivity.EXTRA_FILE_TYPE, fileType.name)
        }
        startActivity(intent)
    }
}

