package com.example.deteksikanker.ui.home

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    private val _currentImageUri = MutableLiveData<Uri?>(null)
    val currentImageUri: LiveData<Uri?> get() = _currentImageUri

    private var previousImageUri: Uri? = null

    fun setImageUri(uri: Uri?) {
        previousImageUri = _currentImageUri.value
        _currentImageUri.value = uri
    }

    fun restorePreviousImageUri() {
        _currentImageUri.value = previousImageUri
    }

    fun clearImageUri() {
        previousImageUri = null
        _currentImageUri.value = null
    }
}

