package com.example.catchup.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.catchup.R
import com.example.catchup.shared.library.LibraryCreator.Companion.SELECTED_SAVE_FILE
import kotlinx.android.synthetic.main.main_row_item.view.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception

class CategoryAdapter(val context: Context) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    data class Category(
        val name: String,
        var selected: Boolean = false
    )

    private val categories = mutableListOf<Category>()
    private var selectedCount = 0
    private val MAX_SELECTED_COUNT = 4

    fun addList(list: List<String>) {
        val selected = mutableListOf<String>()
        try {
            val input: FileInputStream = context.openFileInput(SELECTED_SAVE_FILE)
            selected.addAll(0, input.readBytes().toString(Charsets.UTF_8).split(", "))
            input.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (selected.size != 0) {
            var i = 0
            list.sortedWith(String.CASE_INSENSITIVE_ORDER).forEach { string ->
                if (i < selected.size && selected[i] == string) {
                    i++
                    categories.add(Category(string, true))
                } else {
                    categories.add(Category(string))
                }
            }
        } else {
            list.sortedWith(String.CASE_INSENSITIVE_ORDER).forEach { string ->
                categories.add(Category(string))
            }
        }
        notifyItemRangeChanged(0, categories.size)
    }

    private fun getSelectedCount(): Int {
        var count = 0
        categories.forEach { category ->
            if (category.selected) count++
        }
        return count
    }

    fun getSelectedItems(): List<String> {
        val list = mutableListOf<String>()
        categories.forEach {
            if (it.selected)
                list.add(it.name)
        }
        return list
    }

    private fun getSelectedItemsForStorage(): String {
        var str = ""
        var first = true
        categories.forEach {
            if (it.selected) {
                if (first) {
                    str += it.name
                    first = false
                } else {
                    str += ", ${it.name}"
                }
            }
        }
        return str
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
        holder.tvTitle.text = category.name
        holder.tvTitle.isSelected = category.selected
        holder.tvTitle.setOnClickListener { view ->
            var selectionChanged = false

            if (view.isSelected) {
                selectionChanged = true
                selectedCount--
            } else {
                if (selectedCount < MAX_SELECTED_COUNT) {
                    selectionChanged = true
                    selectedCount++
                }
            }

            if (selectionChanged) {
                view.isSelected = !view.isSelected
                category.selected = !category.selected
                val content = getSelectedItemsForStorage()
                val out: FileOutputStream = context.openFileOutput(SELECTED_SAVE_FILE, Context.MODE_PRIVATE)
                out.write(content.toByteArray())
                out.close()
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var category: Category? = null
        val tvTitle: TextView = view.tvTitle
    }
}