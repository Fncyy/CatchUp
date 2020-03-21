package com.example.catchup.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.catchup.R
import kotlinx.android.synthetic.main.main_row_item.view.*

class CategoryAdapter : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var categories = listOf<String>()

    fun addList(list: List<String>) {
        categories = list.sortedWith(String.CASE_INSENSITIVE_ORDER)
        notifyItemRangeChanged(0, categories.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.main_row_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = categories.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.category = category
        holder.tvTitle.text = category
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var category: String? = null
        val tvTitle: TextView = view.tvTitle
    }
}