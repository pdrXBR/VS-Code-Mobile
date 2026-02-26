package com.vscodemobile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class FileExplorer(private val context: Context, private val onFileClick: (File) -> Unit) {

    private var currentDirectory: File = File("/storage/emulated/0")
    private val files = mutableListOf<File>()

    fun loadFiles(path: String): List<File> {
        currentDirectory = File(path)
        files.clear()
        val list = currentDirectory.listFiles()?.sortedBy { it.name } ?: emptyList()
        files.addAll(list)
        return files
    }

    fun getCurrentDirectory(): File = currentDirectory

    fun getFiles(): List<File> = files

    inner class FileAdapter(private val files: List<File>) :
        RecyclerView.Adapter<FileAdapter.ViewHolder>() {

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val name: TextView = itemView.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val file = files[position]
            holder.name.text = if (file.isDirectory) "📁 ${file.name}" else "📄 ${file.name}"
            holder.itemView.setOnClickListener {
                onFileClick(file)
            }
        }

        override fun getItemCount() = files.size
    }
}