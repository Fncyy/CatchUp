package com.example.catchup

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.catchup.adapter.CategoryAdapter
import com.example.catchup.adapter.NewsAdapter
import com.example.catchup.model.CategoryResponse
import com.example.catchup.model.NewsResponse
import com.example.catchup.network.CurrentsInteractor
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.internal.lockAndWaitNanos
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private val currentsInteractor = CurrentsInteractor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        categoryAdapter = CategoryAdapter(this)
        newsAdapter = NewsAdapter()
        rvMain.adapter = categoryAdapter
        layoutManager = LinearLayoutManager(this)
        rvMain.layoutManager = layoutManager
        getAvailableCategories()

        testTTS()
    }

    private lateinit var tts: TextToSpeech
    private lateinit var file: File

    companion object {
        val TTS_TEST = "ttsTest"
    }

    private fun testTTS() {
        val filePath = applicationContext.filesDir.path + "/hello.wav"
        file = File(filePath)
        tts = TextToSpeech(this, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale.UK
            }
        })

        val result = when(tts.synthesizeToFile("A short sentece for testing", null, file, null)) {
            TextToSpeech.ERROR -> "error"
            TextToSpeech.SUCCESS -> "success"
            else -> "neither somehow"
        }
        Log.d(TTS_TEST, "synthesizeToFile queue result: ${result}")
        tts.shutdown()
    }

    private fun getLatestNews() {
        currentsInteractor.getLatestNews(
            onSuccess = this::showNews,
            onError = this::showError
        )
    }

    private fun getAvailableCategories() {
        currentsInteractor.getAvailableCategories(
            onSuccess = this::showCategories,
            onError = this::showError
        )
    }

    private fun showCategories(response: CategoryResponse) {
        categoryAdapter.addList(response.categories)

    }

    private fun showNews(response: NewsResponse) {
        newsAdapter.addList(response.news)
    }

    private fun showError(e: Throwable) {
        e.printStackTrace()
    }
}
