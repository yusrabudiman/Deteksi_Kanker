package com.example.deteksikanker.data.retrofit

import com.example.deteksikanker.data.response.CancerApiResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiServices {
    @GET("top-headlines")
    fun getCancerArticles(
        @Query("q") query: String = "cancer",
        @Query("category") category: String = "health",
        @Query("language") language: String = "en",
        @Query("apiKey") apiKey: String = "9f5290071986464788b5c4ce243d9160"
    ): Call<CancerApiResponse>
}
