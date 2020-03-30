package com.example.catchup

import android.app.ActionBar
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.catchup.adapter.CategoryAdapter
import com.example.catchup.adapter.NewsAdapter
import com.example.catchup.model.CategoryResponse
import com.example.catchup.model.NewsResponse
import com.example.catchup.network.CurrentsInteractor
import com.example.catchup.network.isConnected
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.internal.lockAndWaitNanos
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private val currentsInteractor = CurrentsInteractor()
    private val SAVED_NEWS_COUNT = 3

    private lateinit var tts: TextToSpeech
    private lateinit var file: File
    private lateinit var filePath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        categoryAdapter = CategoryAdapter(this)
        newsAdapter = NewsAdapter()
        rvMain.adapter = categoryAdapter
        layoutManager = LinearLayoutManager(this)
        rvMain.layoutManager = layoutManager
        getAvailableCategories()
        //getLatestNews()

        filePath = applicationContext.filesDir.path

        //testTTS()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuReload -> getAvailableCategories()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun testTTS() {
        //val filePath = applicationContext.filesDir.path + "/hello.wav"
        file = File(filePath)
        tts = TextToSpeech(this, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale.UK

                categoryAdapter.getSelectedItems().forEach {
                    currentsInteractor.getCategory(
                        onSuccessWithString = this::saveNewsByCategory,
                        onError = this::showError,
                        category = it
                    )
                }
            }
        })
    }

    private fun saveNewsByCategory(response: NewsResponse, category: String) {
        for (i in 0 until SAVED_NEWS_COUNT) {
            file = File("$filePath/${category}_$i.wav")
            tts.synthesizeToFile(response.news[i].description, null, file, null)
        }
    }

    override fun onStop() {
        //tts.shutdown()
        super.onStop()
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
