package com.example.catchup

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.catchup.adapter.CategoryAdapter
import com.example.catchup.adapter.NewsAdapter
import com.example.catchup.shared.library.BrowseTree
import com.example.catchup.shared.model.CategoryResponse
import com.example.catchup.shared.model.NewsResponse
import com.example.catchup.shared.network.CurrentsInteractor
import kotlinx.android.synthetic.main.activity_main.*
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

    private lateinit var browseTree: BrowseTree

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

        browseTree = BrowseTree(this)

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
        categoryAdapter.setList(response.categories)
    }

    private fun showNews(response: NewsResponse) {
        newsAdapter.addList(response.news)
    }

    private fun showError(e: Throwable) {
        e.printStackTrace()
    }
}
