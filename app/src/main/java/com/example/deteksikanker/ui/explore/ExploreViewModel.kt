package com.example.deteksikanker.ui.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.deteksikanker.data.response.ArticlesItem
import com.example.deteksikanker.data.response.CancerApiResponse
import com.example.deteksikanker.data.retrofit.ApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExploreViewModel : ViewModel() {

    private val _articles = MutableLiveData<List<ArticlesItem>>()
    val articles: LiveData<List<ArticlesItem>> get() = _articles

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()

    fun fetchCancerArticles() {
        _isLoading.value = true  // Start loading
        val apiServices = ApiConfig.getApiServices()
        val call = apiServices.getCancerArticles()

        call.enqueue(object : Callback<CancerApiResponse> {
            override fun onResponse(
                call: Call<CancerApiResponse>,
                response: Response<CancerApiResponse>
            ) {
                _isLoading.value = false  // Stop loading

                if (response.isSuccessful) {
                    // Filter out articles with placeholder "[Removed]" data
                    _articles.value = response.body()?.articles
                        ?.filter { it?.title != "[Removed]" && it?.description != "[Removed]" && it?.content != "[Removed]" }
                        ?.filterNotNull() ?: emptyList()

                    _errorMessage.value = if (_articles.value.isNullOrEmpty()) {
                        "Tidak ada artikel yang ditemukan."
                    } else null
                } else {
                    _errorMessage.value = "Error ${response.code()}: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<CancerApiResponse>, t: Throwable) {
                _isLoading.value = false
                _errorMessage.value = "Gagal terhubung: ${t.localizedMessage}"
            }
        })
    }
}

