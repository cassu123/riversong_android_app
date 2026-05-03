package com.riversongai.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.riversongai.data.model.MemoryPreference
import com.riversongai.databinding.ItemMemoryPreferenceBinding

class PreferenceAdapter : ListAdapter<MemoryPreference, PreferenceAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMemoryPreferenceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemMemoryPreferenceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pref: MemoryPreference) {
            binding.chipCategory.text = pref.category
            binding.textViewValue.text = pref.value
            val confPct = (pref.confidence * 100).toInt()
            binding.progressConfidence.progress = confPct
            binding.textViewConfidence.text = "Confidence: $confPct%"
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<MemoryPreference>() {
        override fun areItemsTheSame(oldItem: MemoryPreference, newItem: MemoryPreference) = 
            oldItem.category == newItem.category && oldItem.value == newItem.value
        override fun areContentsTheSame(oldItem: MemoryPreference, newItem: MemoryPreference) = oldItem == newItem
    }
}
