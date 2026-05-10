package com.riversongai.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.riversongai.R
import com.riversongai.data.model.MemorySummary
import java.text.SimpleDateFormat
import java.util.*

class SummaryAdapter(private val onDelete: (MemorySummary) -> Unit) : ListAdapter<MemorySummary, SummaryAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_memory_summary, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position), onDelete)

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvSummary: TextView = view.findViewById(R.id.textViewSummaryText)
        private val tvDate: TextView = view.findViewById(R.id.textViewSummaryDate)
        private val tvTtl: TextView = view.findViewById(R.id.textViewSummaryTtl)
        private val tvExpires: TextView = view.findViewById(R.id.textViewSummaryExpires)
        private val btnDelete: ImageButton = view.findViewById(R.id.buttonDeleteSummary)

        private val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        private val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)

        fun bind(summary: MemorySummary, onDelete: (MemorySummary) -> Unit) {
            tvSummary.text = summary.summary
            tvTtl.text = "TTL: ${summary.ttlSetting}"
            
            tvDate.text = try {
                if (!summary.createdAt.isNullOrBlank()) {
                    val date = inputFormat.parse(summary.createdAt)
                    if (date != null) outputFormat.format(date) else ""
                } else ""
            } catch (e: Exception) { "" }

            tvExpires.text = try {
                if (!summary.expiresAt.isNullOrBlank()) {
                    val date = inputFormat.parse(summary.expiresAt)
                    if (date != null) "Expires: ${outputFormat.format(date)}" else "Never expires"
                } else "Never expires"
            } catch (e: Exception) { "Never expires" }

            btnDelete.setOnClickListener { onDelete(summary) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MemorySummary>() {
            override fun areItemsTheSame(a: MemorySummary, b: MemorySummary) = a.id == b.id
            override fun areContentsTheSame(a: MemorySummary, b: MemorySummary) = a == b
        }
    }
}
