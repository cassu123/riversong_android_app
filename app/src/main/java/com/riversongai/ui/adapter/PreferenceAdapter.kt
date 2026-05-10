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
import com.riversongai.data.model.MemoryPreference
import java.text.SimpleDateFormat
import java.util.*

class PreferenceAdapter(private val onDelete: (MemoryPreference) -> Unit) : ListAdapter<MemoryPreference, PreferenceAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_memory_preference, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position), onDelete)

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvCategory: TextView = view.findViewById(R.id.textViewPrefCategory)
        private val tvValue: TextView = view.findViewById(R.id.textViewPrefValue)
        private val tvConfidence: TextView = view.findViewById(R.id.textViewPrefConfidence)
        private val tvDate: TextView = view.findViewById(R.id.textViewPrefDate)
        private val btnDelete: ImageButton = view.findViewById(R.id.buttonDeletePref)

        private val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        private val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)

        fun bind(pref: MemoryPreference, onDelete: (MemoryPreference) -> Unit) {
            tvCategory.text = pref.category
            tvValue.text = pref.value
            tvConfidence.text = "Confidence: ${pref.confidence}"
            
            tvDate.text = try {
                if (!pref.lastUpdated.isNullOrBlank()) {
                    val date = inputFormat.parse(pref.lastUpdated)
                    if (date != null) outputFormat.format(date) else ""
                } else ""
            } catch (e: Exception) { "" }

            btnDelete.setOnClickListener { onDelete(pref) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MemoryPreference>() {
            override fun areItemsTheSame(a: MemoryPreference, b: MemoryPreference) = a.id == b.id
            override fun areContentsTheSame(a: MemoryPreference, b: MemoryPreference) = a == b
        }
    }
}
