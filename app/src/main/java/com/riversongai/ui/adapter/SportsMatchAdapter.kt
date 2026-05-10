package com.riversongai.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.riversongai.R
import com.riversongai.data.model.SportsMatch
import com.riversongai.databinding.ItemSportsMatchBinding

class SportsMatchAdapter : ListAdapter<SportsMatch, SportsMatchAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSportsMatchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemSportsMatchBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(match: SportsMatch) {
            binding.textViewLeague.text = match.league
            binding.textViewHomeTeam.text = match.homeTeam
            binding.textViewAwayTeam.text = match.awayTeam
            
            binding.imageViewHomeLogo.load(match.homeBadge) { placeholder(R.drawable.ic_public); error(R.drawable.ic_public) }
            binding.imageViewAwayLogo.load(match.awayBadge) { placeholder(R.drawable.ic_public); error(R.drawable.ic_public) }

            if (match.homeScore == null || match.awayScore == null) {
                binding.textViewScore.isVisible = false
                binding.textViewVS.isVisible = true
            } else {
                binding.textViewScore.isVisible = true
                binding.textViewVS.isVisible = false
                binding.textViewScore.text = "${match.homeScore} - ${match.awayScore}"
            }

            binding.chipMatchStatus.text = match.status
            when {
                match.status.contains("LIVE", true) || match.status.contains("1st", true) || match.status.contains("2nd", true) -> {
                    binding.chipMatchStatus.setChipBackgroundColorResource(com.google.android.material.R.color.material_dynamic_tertiary80)
                }
                match.finished -> {
                    binding.chipMatchStatus.setChipBackgroundColorResource(com.google.android.material.R.color.material_dynamic_secondary80)
                }
                else -> {
                    binding.chipMatchStatus.setChipBackgroundColorResource(com.google.android.material.R.color.material_dynamic_neutral80)
                }
            }

            binding.textViewMatchDate.text = if (match.time != null) "${match.date} ${match.time}" else match.date
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SportsMatch>() {
        override fun areItemsTheSame(oldItem: SportsMatch, newItem: SportsMatch) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SportsMatch, newItem: SportsMatch) = oldItem == newItem
    }
}
