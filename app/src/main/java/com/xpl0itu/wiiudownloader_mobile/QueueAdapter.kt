package com.xpl0itu.wiiudownloader_mobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class QueueAdapter(private val onItemClick: (gtitlesWrapper.TitleEntry) -> Unit) : RecyclerView.Adapter<QueueAdapter.QueueViewHolder>() {

    private var itemList: MutableList<gtitlesWrapper.TitleEntry> = mutableListOf()

    fun setItems(items: List<gtitlesWrapper.TitleEntry>) {
        itemList.clear()
        itemList.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueueViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.queue_item, parent, false)
        return QueueViewHolder(view)
    }

    override fun onBindViewHolder(holder: QueueViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class QueueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)

        fun bind(item: gtitlesWrapper.TitleEntry) {
            titleTextView.text = item.name
        }
    }
}
