package com.example.deteksikanker.ui.resultactivity

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
//noinspection ExifInterface
import android.media.ExifInterface
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
            binding.resultText.text = "Result: $title"
            binding.confidenceScore.text = "Confidence: ${String.format("%.2f", confidenceScore * 100)}%"

            val bitmap = BitmapFactory.decodeFile(imagePath)
            binding.resultImage.setImageBitmap(bitmap)
        } else {
            val imageUriString = intent.getStringExtra("IMAGE_URI")
            imageUriString?.let {
                val imageUri = Uri.parse(it)
                adjustImageRotationAndAnalyze(imageUri)
            } ?: run {
                binding.resultText.text = "No image available"
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun adjustImageRotationAndAnalyze(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            if (inputStream == null) {
                binding.resultText.text = "Error: Image could not be opened"
                return
            }

            val exif = ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }

            val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
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
        }
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun analyzeImage(bitmap: Bitmap) {
        try {
            val model = CancerClassification.newInstance(this)

            // Membuat TensorImage dari bitmap
            val image = TensorImage.fromBitmap(bitmap)

            // Memproses gambar dan mengambil hasil prediksi
            val outputs = model.process(image)
            val probability = outputs.probabilityAsCategoryList

            if (probability.isEmpty()) {
                binding.resultText.text = "Error: No prediction available"
                binding.confidenceScore.text = ""
                return
            }

            // Menampilkan kategori dengan skor tertinggi
            val topCategory = probability.maxByOrNull { it.score }

            topCategory?.let {
                binding.resultText.text = "Result: ${it.label}"
                binding.confidenceScore.text = "Confidence: ${String.format("%.2f", it.score * 100)}%"

                // Menyimpan gambar dan hasil deteksi
                val imagePath = saveImageToInternalStorage(Uri.parse(intent.getStringExtra("IMAGE_URI") ?: ""))
                saveDetectionResult(it.label, it.score, imagePath)
            } ?: run {
                binding.resultText.text = "Error: No prediction available"
                binding.confidenceScore.text = ""
            }

            model.close()
        } catch (e: Exception) {
            e.printStackTrace()
            binding.resultText.text = "Error analyzing image: ${e.message}"
            binding.confidenceScore.text = ""
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
