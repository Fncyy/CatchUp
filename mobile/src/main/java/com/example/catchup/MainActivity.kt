package com.example.catchup

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.catchup.adapter.CategoryAdapter
import com.example.catchup.adapter.NewsAdapter
import com.example.catchup.model.CategoryResponse
import com.example.catchup.model.NewsResponse
import kotlinx.android.synthetic.main.activity_main.*

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
    }

    private fun getLatestNews() {
        currentsInteractor.getLatestNews(onSuccess = this::showNews, onError = this::showError)
    }

    private fun getAvailableCategories() {
        currentsInteractor.getAvailableCategories(onSuccess = this::showCategories, onError = this::showError)
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
