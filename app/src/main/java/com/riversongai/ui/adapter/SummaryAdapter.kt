package com.riversongai.ui.adapter

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.riversongai.R
import com.riversongai.data.model.MemorySummary
import com.riversongai.databinding.ItemMemorySummaryBinding
import java.util.Date

class SummaryAdapter : ListAdapter<MemorySummary, SummaryAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMemorySummaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemMemorySummaryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(summary: MemorySummary) {
            binding.textViewDate.text = DateFormat.getMediumDateFormat(binding.root.context).format(Date(summary.createdAt))
            binding.textViewSummary.text = summary.summary
            binding.chipTtl.text = summary.ttl

            val now = System.currentTimeMillis()
            val weekFromNow = now + 7 * 24 * 3600 * 1000L
            val dayFromNow = now + 24 * 3600 * 1000L

            val colorRes = when {
                summary.expiresAt > weekFromNow -> R.color.river_song_success_container
                summary.expiresAt > dayFromNow -> R.color.river_song_warning_container
                else -> R.color.river_song_error_container
            }
            binding.chipTtl.setChipBackgroundColorResource(colorRes)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<MemorySummary>() {
        override fun areItemsTheSame(oldItem: MemorySummary, newItem: MemorySummary) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: MemorySummary, newItem: MemorySummary) = oldItem == newItem
    }
}
