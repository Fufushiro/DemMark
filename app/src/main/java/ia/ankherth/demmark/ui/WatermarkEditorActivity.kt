package ia.ankherth.demmark.ui

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ia.ankherth.demmark.databinding.ActivityWatermarkEditorBinding
import ia.ankherth.demmark.domain.model.WatermarkPosition
import ia.ankherth.demmark.ui.viewmodel.WatermarkEditorViewModel

class WatermarkEditorActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FILE_URI = "extra_file_uri"
        const val EXTRA_FILE_TYPE = "extra_file_type"
    }

    private lateinit var binding: ActivityWatermarkEditorBinding
    private val viewModel: WatermarkEditorViewModel by viewModels()
    private var textWatcher: TextWatcher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWatermarkEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val fileUriString = intent.getStringExtra(EXTRA_FILE_URI)
        val fileTypeString = intent.getStringExtra(EXTRA_FILE_TYPE)

        if (fileUriString != null && fileTypeString != null) {
            val uri = Uri.parse(fileUriString)
            val fileType = ia.ankherth.demmark.domain.model.FileType.valueOf(fileTypeString)
            viewModel.initialize(uri, fileType)
        } else {
            Toast.makeText(this, "Error: No se pudo cargar el archivo", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.previewBitmap.observe(this) { bitmap ->
            bitmap?.let {
                binding.previewImageView.setImageBitmap(it)
            }
        }

        viewModel.watermarkConfig.observe(this) { config ->
            config?.let {
                // Remover el listener temporalmente para evitar actualizaciones infinitas
                textWatcher?.let { binding.watermarkTextEditText.removeTextChangedListener(it) }
                
                val currentText = binding.watermarkTextEditText.text?.toString() ?: ""
                if (currentText != it.text) {
                    binding.watermarkTextEditText.setText(it.text)
                }
                binding.fontSizeSeekBar.progress = it.fontSize.toInt()
                binding.opacitySeekBar.progress = it.opacity
                updateFontSizeDisplay(it.fontSize)
                updateOpacityDisplay(it.opacity)
                
                // Restaurar el listener
                textWatcher?.let { binding.watermarkTextEditText.addTextChangedListener(it) }
            }
        }

        viewModel.isSaving.observe(this) { isSaving ->
            binding.fab.isEnabled = !isSaving
        }

        viewModel.saveResult.observe(this) { result ->
            result?.fold(
                onSuccess = { uri ->
                    val message = getString(
                        ia.ankherth.demmark.R.string.file_saved_to,
                        uri.toString()
                    )
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    viewModel.clearSaveResult()
                },
                onFailure = { error ->
                    Toast.makeText(
                        this,
                        "${getString(ia.ankherth.demmark.R.string.error)}: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.clearSaveResult()
                }
            )
        }
    }

    private fun setupClickListeners() {
        // Texto de marca de agua
        textWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateText(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        }
        binding.watermarkTextEditText.addTextChangedListener(textWatcher)

        // Tamaño de fuente
        binding.fontSizeSeekBar.setOnSeekBarChangeListener(
            object : android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: android.widget.SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        val size = progress.toFloat().coerceIn(12f, 200f)
                        viewModel.updateFontSize(size)
                        updateFontSizeDisplay(size)
                    }
                }

                override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
            }
        )

        // Opacidad
        binding.opacitySeekBar.setOnSeekBarChangeListener(
            object : android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: android.widget.SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        viewModel.updateOpacity(progress)
                        updateOpacityDisplay(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
            }
        )

        // Colores
        binding.colorBlackButton.setOnClickListener {
            viewModel.updateColor(Color.BLACK)
        }
        binding.colorRedButton.setOnClickListener {
            viewModel.updateColor(Color.WHITE)
        }
        binding.colorBlueButton.setOnClickListener {
            viewModel.updateColor(Color.BLUE)
        }

        // Posiciones
        binding.positionCenterButton.setOnClickListener {
            viewModel.updatePosition(WatermarkPosition.CENTER)
        }
        binding.positionDiagonalButton.setOnClickListener {
            viewModel.updatePosition(WatermarkPosition.DIAGONAL)
        }
        binding.positionTopLeftButton.setOnClickListener {
            viewModel.updatePosition(WatermarkPosition.TOP_LEFT)
        }
        binding.positionBottomRightButton.setOnClickListener {
            viewModel.updatePosition(WatermarkPosition.BOTTOM_RIGHT)
        }

        binding.fab.setOnClickListener {
            viewModel.saveFile()
        }
    }

    private fun updateFontSizeDisplay(size: Float) {
        binding.fontSizeValueTextView.text = size.toInt().toString()
    }

    private fun updateOpacityDisplay(opacity: Int) {
        binding.opacityValueTextView.text = "$opacity%"
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remover el TextWatcher para evitar fugas de memoria
        textWatcher?.let {
            binding.watermarkTextEditText.removeTextChangedListener(it)
            textWatcher = null
        }
    }
}


