package com.example.deteksikanker.ui.resultactivity

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import com.example.deteksikanker.data.local.database.HistoryCancerRecord
import com.example.deteksikanker.databinding.ActivityResultBinding
import com.example.deteksikanker.ml.CancerClassification
import com.example.deteksikanker.ui.history.HistoryViewModel
import org.tensorflow.lite.support.image.TensorImage
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.Date

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private val historyViewModel: HistoryViewModel by viewModels()

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Result Cancer"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val title = intent.getStringExtra("title")
        val confidenceScore = intent.getFloatExtra("confidenceScore", -1f)
        val imagePath = intent.getStringExtra("image")

        if (title != null && confidenceScore != -1f && imagePath != null) {
            binding.resultText.text = "$title ${String.format("%.2f", confidenceScore * 100)}%"
            val bitmap = BitmapFactory.decodeFile(imagePath)
            binding.resultImage.setImageBitmap(bitmap)
        } else {
            val imageUriString = intent.getStringExtra("IMAGE_URI")
            if (imageUriString.isNullOrEmpty()) {
                binding.resultText.text = "No image available"
            } else {
                val imageUri = Uri.parse(imageUriString)
                adjustImageRotationAndAnalyze(imageUri)
            }
        }
    }


    @SuppressLint("SetTextI18n", "Recycle")
    private fun adjustImageRotationAndAnalyze(uri: Uri) {
        var inputStream: InputStream? = null
        try {
            inputStream = contentResolver.openInputStream(uri)
            if (inputStream == null) {
                binding.resultText.text = "Error: Image could not be opened"
                return
            }
            val matrix = Matrix()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            if (bitmap == null) {
                binding.resultText.text = "Error: Failed to decode the image"
                return
            }
            val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            binding.resultImage.setImageBitmap(rotatedBitmap)
            analyzeImage(rotatedBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            binding.resultText.text = "Error adjusting rotation: ${e.message}"
        } finally {
            inputStream?.close()
        }
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun analyzeImage(bitmap: Bitmap) {
        try {
            val model = CancerClassification.newInstance(this)
            val image = TensorImage.fromBitmap(bitmap)
            val outputs = model.process(image)
            val probability = outputs.probabilityAsCategoryList

            if (probability.isEmpty()) {
                binding.resultText.text = "Error: No prediction available"
                return
            }
            val topCategory = probability.maxByOrNull { it.score }

            topCategory?.let {
                binding.resultText.text = "${it.label} ${String.format("%.2f", it.score * 100)}%"
                val imagePath = saveImageToInternalStorage(Uri.parse(intent.getStringExtra("IMAGE_URI") ?: ""))
                saveDetectionResult(it.label, it.score, imagePath)
            } ?: run {
                binding.resultText.text = "Error: No prediction available"
            }

            model.close()
        } catch (e: Exception) {
            e.printStackTrace()
            binding.resultText.text = "Error analyzing image: ${e.message}"
        }
    }

    private fun saveDetectionResult(label: String, confidenceScore: Float, imagePath: String) {
        val date = Date().time

        val record = HistoryCancerRecord(
            title = label,
            confidenceScore = confidenceScore,
            date = date,
            image = imagePath
        )
        historyViewModel.insertRecord(record)
    }

    @SuppressLint("SetTextI18n")
    private fun saveImageToInternalStorage(uri: Uri): String {
        try {
            val file = File(filesDir, "${System.currentTimeMillis()}.jpg")
            val outputStream: OutputStream = FileOutputStream(file)
            val inputStream = contentResolver.openInputStream(uri)
            if (inputStream == null) {
                binding.resultText.text = "Error: Unable to open image stream"
                return ""
            }

            val bitmap = BitmapFactory.decodeStream(inputStream)
            if (bitmap == null) {
                binding.resultText.text = "Error: Unable to decode image"
                return ""
            }

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            binding.resultText.text = "Error saving image: ${e.message}"
            return ""
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
