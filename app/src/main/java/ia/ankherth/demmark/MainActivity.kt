package ia.ankherth.demmark

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import ia.ankherth.demmark.data.FileType
import ia.ankherth.demmark.viewmodel.WatermarkViewModel

class MainActivity : AppCompatActivity() {
    
    private lateinit var viewModel: WatermarkViewModel
    
    private lateinit var previewImage: ImageView
    private lateinit var watermarkInput: EditText
    private lateinit var selectImageBtn: Button
    private lateinit var saveImageBtn: Button
    private lateinit var applyWatermarkBtn: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    private lateinit var placeholderText: TextView
    private lateinit var opacitySeek: android.widget.SeekBar
    
    private var currentFileType: FileType? = null
    
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            currentFileType = FileType.IMAGE
            // Persist permission for future access
            try {
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {
            }
            viewModel.setFileUri(it, FileType.IMAGE)
            placeholderText.visibility = View.GONE
            previewImage.visibility = View.VISIBLE
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        viewModel = ViewModelProvider(this).get(WatermarkViewModel::class.java)
        
        initializeViews()
        setupObservers()
        setupListeners()
    }
    
    override fun onStart() {
        super.onStart()
        applyWatermarkBtn.isEnabled = true
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // No extra unregister required for ActivityResultLaunchers here.
    }
    
    private fun initializeViews() {
        previewImage = findViewById(R.id.previewImage)
        watermarkInput = findViewById(R.id.watermarkInput)
        selectImageBtn = findViewById(R.id.selectImageBtn)
        saveImageBtn = findViewById(R.id.saveImageBtn)
        applyWatermarkBtn = findViewById(R.id.applyWatermarkBtn)
        opacitySeek = findViewById(R.id.opacitySeek)
        progressBar = findViewById(R.id.progressBar)
        statusText = findViewById(R.id.statusText)
        placeholderText = findViewById(R.id.placeholderText)
        
        previewImage.visibility = View.GONE
        progressBar.visibility = View.GONE
        statusText.visibility = View.GONE
    }
    
    private fun setupObservers() {
        viewModel.preview.observe(this) { bitmap ->
            if (bitmap != null) {
                previewImage.setImageBitmap(bitmap)
                previewImage.visibility = View.VISIBLE
            }
        }
        
        viewModel.state.observe(this) { state ->
            if (state.isProcessing) {
                progressBar.visibility = View.VISIBLE
                statusText.visibility = View.VISIBLE
                statusText.text = state.statusMessage
                applyWatermarkBtn.isEnabled = false
            } else {
                progressBar.visibility = View.GONE
                applyWatermarkBtn.isEnabled = true
            }
        }
        
        viewModel.statusUpdate.observe(this) { message ->
            statusText.visibility = View.VISIBLE
            statusText.text = message
        }
    }
    
    private fun setupListeners() {
        selectImageBtn.setOnClickListener {
            // Use OpenDocument to allow persistable permissions
            imagePickerLauncher.launch(arrayOf("image/*"))
        }
        
        watermarkInput.setOnTextChangedListener { text ->
            viewModel.setWatermarkText(text.toString())
        }

        opacitySeek.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                // update ViewModel state opacity
                viewModel.setOpacity(progress)
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })

        applyWatermarkBtn.setOnClickListener {
            viewModel.applyWatermark()
        }

        saveImageBtn.setOnClickListener {
            // Save current preview only; does not apply watermark
            viewModel.savePreviewToGallery()
        }
    }
}

private inline fun EditText.setOnTextChangedListener(crossinline onTextChanged: (CharSequence) -> Unit) {
    addTextChangedListener(object : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            s?.let { onTextChanged(it) }
        }
        override fun afterTextChanged(s: android.text.Editable?) {}
    })
}
