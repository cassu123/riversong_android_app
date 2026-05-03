package com.riversongai.ui.adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
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
            binding.textViewLeague.text = match.leagueName
            binding.textViewHomeTeam.text = match.homeTeam
            binding.textViewAwayTeam.text = match.awayTeam
            
            if (match.status == "NS") {
                binding.textViewScore.text = "vs"
                binding.chipMatchStatus.text = "NS"
                binding.chipMatchStatus.setChipBackgroundColorResource(R.color.river_song_surface_variant)
            } else {
                binding.textViewScore.text = "${match.homeScore} - ${match.awayScore}"
                binding.chipMatchStatus.text = match.status
                if (match.status == "LIVE") {
                    binding.chipMatchStatus.setChipBackgroundColorResource(R.color.river_song_error_container)
                } else {
                    binding.chipMatchStatus.setChipBackgroundColorResource(R.color.river_song_success_container)
                }
            }

            binding.textViewMatchDate.text = DateUtils.getRelativeTimeSpanString(
                match.kickoff,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SportsMatch>() {
        override fun areItemsTheSame(oldItem: SportsMatch, newItem: SportsMatch) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SportsMatch, newItem: SportsMatch) = oldItem == newItem
    }
}
