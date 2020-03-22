package com.example.catchup.network

import com.example.catchup.model.CategoryResponse
import com.example.catchup.model.NewsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrentsAPI {

    companion object {
        const val ENDPOINT_URL = "https://api.currentsapi.services/v1/"
        const val LATEST_NEWS_URL = "latest-news"
        const val SEARCH_URL = "search"
        const val CATEGORY_URL = "available/categories"
        const val API_KEY = "mBqiPHzKXZKRq64JiB3HeMXF3SvRXX7RXkkNX-8mYl_M9Ugi"
    }

    @GET(LATEST_NEWS_URL)
    fun getLatestNews(
        @Query("apiKey") apiKey: String = API_KEY
    ): Call<NewsResponse>

    @GET(CATEGORY_URL)
    fun getAvailableCategories(): Call<CategoryResponse>

    @GET(SEARCH_URL)
    fun getCategory(
        @Query("category") category: String,
        @Query("apiKey") apiKey: String = API_KEY
    ): Call<NewsResponse>
}