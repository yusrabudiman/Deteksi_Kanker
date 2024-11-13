package com.example.deteksikanker.ui.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
//noinspection ExifInterface
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.deteksikanker.R
import com.example.deteksikanker.databinding.FragmentHomeBinding
import com.example.deteksikanker.ui.resultactivity.ResultActivity
import com.yalantis.ucrop.UCrop

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var currentImageUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                currentImageUri = uri
                startCrop(uri)
            }
        } else {
            showToast("Failed to select image.")
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startGallery()
        } else {
            showToast("Permission denied to access gallery.")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.galleryButton.setOnClickListener {
            checkGalleryPermissionAndOpen()
        }
        binding.analyzeButton.setOnClickListener {
            analyzeImage()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkGalleryPermissionAndOpen() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(permission)
        } else {
            startGallery()
        }
    }

    private fun startGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        galleryLauncher.launch(intent)
    }

    private fun startCrop(uri: Uri) {
        currentImageUri = uri
        val destinationUri = Uri.fromFile(requireContext().cacheDir.resolve("cropped_image.jpg"))
        UCrop.of(uri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(1000, 1000)
            .start(requireContext(), this)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            data?.let {
                val resultUri = UCrop.getOutput(data)
                resultUri?.let { uri ->
                    val rotatedBitmap = rotateImage(uri)
                    currentImageUri = saveBitmapToUri(rotatedBitmap)
                    rotatedBitmap?.let { showImage(uri) }
                } ?: showToast("Failed to get cropped image.")
            } ?: showToast("Image data is null.")
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            cropError?.printStackTrace()
            showToast("Crop error: ${cropError?.message}")
        }
    }

    private fun rotateImage(uri: Uri, degrees: Float = 0f): Bitmap? {
        return try {
            val originalBitmap = BitmapFactory.decodeStream(requireContext().contentResolver.openInputStream(uri))
            val matrix = Matrix()
            val exif = ExifInterface(requireContext().contentResolver.openInputStream(uri)!!)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                else -> matrix.postRotate(degrees)
            }

            Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Failed to rotate image: ${e.message}")
            null
        }
    }

    private fun saveBitmapToUri(bitmap: Bitmap?): Uri? {
        return try {
            val file = requireContext().cacheDir.resolve("rotated_image.jpg")
            val outStream = file.outputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 90, outStream)
            outStream.flush()
            outStream.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Failed to save image: ${e.message}")
            null
        }
    }

    private fun showImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.ic_place_holder)
            .error(R.drawable.ic_place_holder)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(binding.previewImageView)
        binding.previewImageView.invalidate()
    }

    private fun analyzeImage() {
        if (currentImageUri != null) {
            moveToResult(currentImageUri!!)
            Glide.with(this)
                .load(R.drawable.ic_place_holder)
                .into(binding.previewImageView)
            currentImageUri = null
        } else {
            showToast("No Image Selected for Analysis")
        }
    }

    private fun moveToResult(uri: Uri) {
        val intent = Intent(requireActivity(), ResultActivity::class.java).apply {
            putExtra("IMAGE_URI", uri.toString())
        }
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
