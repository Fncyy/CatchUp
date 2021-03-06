package com.example.catchup.shared.network

import android.os.Handler
import com.example.catchup.shared.model.CategoryResponse
import com.example.catchup.shared.model.NewsResponse
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

    fun getAvailableCategories(
        onSuccess: (CategoryResponse) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val getCategoriesRequest = currentsApi.getAvailableCategories()
        runCallOnBackgroundThread(getCategoriesRequest, onSuccess, null, onError)
    }

    fun getCategory(
        onSuccessWithString: (NewsResponse, String) -> Unit,
        onError: (Throwable) -> Unit,
        category: String
    ) {
        val getNewsRequest = currentsApi.getCategory(category)
        runCallOnBackgroundThreadWithString(getNewsRequest, onSuccessWithString, onError, category)
    }

    private fun <T> runCallOnBackgroundThread(
        call: Call<T>,
        onSuccess: ((T) -> Unit)?,
        onSuccessWithString: ((T, String) -> Unit)?,
        onError: (Throwable) -> Unit,
        string: String = ""
    ) {
        val handler = Handler()
        Thread {
            try {
                val response = call.execute().body()!!
                handler.post {
                    if (onSuccess != null)
                        onSuccess(response)
                    if (onSuccessWithString != null)
                        onSuccessWithString(response, string)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                handler.post { onError(e) }
            }
        }.start()
    }

    private fun <T> runCallOnBackgroundThreadWithString(
        call: Call<T>,
        onSuccess: (T, String) -> Unit,
        onError: (Throwable) -> Unit,
        string: String
    ) {
        val handler = Handler()
        Thread {
            try {
                val response = call.execute().body()!!
                handler.post { onSuccess(response, string) }

            } catch (e: Exception) {
                e.printStackTrace()
                handler.post { onError(e) }
            }
        }.start()
    }
}