package com.example.catchup.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.catchup.R
import com.example.catchup.shared.model.News
import kotlinx.android.synthetic.main.main_news_row.view.*

class NewsAdapter : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {

    private val newsList = mutableListOf<News>()

    fun addList(list: List<News>) {
        newsList += list
        notifyItemRangeChanged(0, newsList.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.main_news_row, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = newsList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val newsItem = newsList[position]
        holder.newsItem = newsItem
        holder.tvTitle.text = newsItem.title
        holder.tvDate.text = newsItem.published
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var newsItem: News? = null
        val tvTitle: TextView = view.tvTitle
        val tvDate: TextView = view.tvDate
    }
}