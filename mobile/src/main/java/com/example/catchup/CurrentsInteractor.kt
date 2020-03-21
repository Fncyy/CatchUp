package com.example.catchup

import android.os.Handler
import com.example.catchup.model.CategoryResponse
import com.example.catchup.model.NewsResponse
import com.example.catchup.network.CurrentsAPI
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CurrentsInteractor {
    private val currentsApi: CurrentsAPI

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(CurrentsAPI.ENDPOINT_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        this.currentsApi = retrofit.create(CurrentsAPI::class.java)
    }

    private fun <T> runCallOnBackgroundThread(
        call: Call<T>,
        onSuccess: (T) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val handler = Handler()
        Thread {
            try {
                val response = call.execute().body()!!
                handler.post { onSuccess(response) }

            } catch (e: Exception) {
                e.printStackTrace()
                handler.post { onError(e) }
            }
        }.start()
    }

    fun getLatestNews(
        onSuccess: (NewsResponse) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val getNewsRequest = currentsApi.getLatestNews()
        runCallOnBackgroundThread(getNewsRequest, onSuccess, onError)
    }

    fun getAvailableCategories(
        onSuccess: (CategoryResponse) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val getCategoriesRequest = currentsApi.getAvailableCategories()
        runCallOnBackgroundThread(getCategoriesRequest, onSuccess, onError)
    }
}