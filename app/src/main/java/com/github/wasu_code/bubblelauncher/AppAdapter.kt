package com.github.wasu_code.bubblelauncher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.wasu_code.bubblelauncher.data.AppEntity

class AppAdapter(
    private val onClick: (AppEntity) -> Unit,
    private val onLong: (View, AppEntity) -> Unit
) : ListAdapter<AppEntity, AppAdapter.VH>(DIFF) {
    companion object {
        val DIFF = object : DiffUtil.ItemCallback<AppEntity>() {
            override fun areItemsTheSame(oldItem: AppEntity, newItem:
            AppEntity) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: AppEntity, newItem:
            AppEntity) = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.bind(item, onClick, onLong)
    }

    private var fullList: List<AppEntity> = emptyList()

    override fun submitList(list: List<AppEntity>?) {
        super.submitList(list)
        if (list != null) fullList = list
    }

    fun filter(query: String) {
        val filtered = if (query.isBlank()) {
            fullList
        } else {
            fullList.filter {
                it.label.contains(query, ignoreCase = true) ||
                        it.packageName.contains(query, ignoreCase = true)
            }
        }
        super.submitList(filtered)
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val label: TextView = view.findViewById(R.id.label)
        private val icon: ImageView = view.findViewById(R.id.icon)
        fun bind(item: AppEntity, onClick: (AppEntity) -> Unit, onLong:
            (View, AppEntity) -> Unit) {
            label.text = item.label
            try {
                val pm = itemView.context.packageManager
                val drawable = pm.getApplicationIcon(item.packageName)
                icon.setImageDrawable(drawable)
            } catch (e: Exception) {
                // ignore
            }
            itemView.setOnClickListener { onClick(item) }
            itemView.setOnLongClickListener { onLong(itemView, item); true }
        }
    }
}
