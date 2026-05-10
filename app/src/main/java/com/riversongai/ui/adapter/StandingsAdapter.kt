package com.riversongai.ui.adapter

import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.riversongai.R
import com.riversongai.data.model.StandingEntry

class StandingsAdapter : ListAdapter<StandingEntry, RecyclerView.ViewHolder>(DIFF) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1

        private val DIFF = object : DiffUtil.ItemCallback<StandingEntry>() {
            override fun areItemsTheSame(a: StandingEntry, b: StandingEntry) = a.teamId == b.teamId
            override fun areContentsTheSame(a: StandingEntry, b: StandingEntry) = a == b
        }
    }

    override fun getItemViewType(position: Int): Int = if (position == 0) TYPE_HEADER else TYPE_ITEM
    override fun getItemCount(): Int {
        val count = super.getItemCount()
        return if (count == 0) 0 else count + 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderViewHolder(inflater.inflate(R.layout.item_standing_header, parent, false))
        } else {
            ItemViewHolder(inflater.inflate(R.layout.item_standing, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            if (super.getItemCount() > 0) holder.bind(super.getItem(0))
        } else if (holder is ItemViewHolder) {
            holder.bind(super.getItem(position - 1))
        }
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val layoutStats: LinearLayout = view.findViewById(R.id.layoutHeaderStats)
        fun bind(firstEntry: StandingEntry) {
            layoutStats.removeAllViews()
            firstEntry.stats.keys.forEach { key ->
                val tv = TextView(itemView.context).apply {
                    layoutParams = LinearLayout.LayoutParams(dpToPx(40), LinearLayout.LayoutParams.WRAP_CONTENT)
                    text = key
                    gravity = Gravity.CENTER
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                    setTextColor(itemView.context.getColor(android.R.color.darker_gray))
                }
                layoutStats.addView(tv)
            }
        }
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvAbbr: TextView = view.findViewById(R.id.textViewStandingAbbr)
        private val tvTeam: TextView = view.findViewById(R.id.textViewStandingTeam)
        private val layoutStats: LinearLayout = view.findViewById(R.id.layoutStandingStats)

        fun bind(entry: StandingEntry) {
            tvAbbr.text = entry.abbr
            tvTeam.text = entry.team
            layoutStats.removeAllViews()
            entry.stats.values.forEach { value ->
                val tv = TextView(itemView.context).apply {
                    layoutParams = LinearLayout.LayoutParams(dpToPx(40), LinearLayout.LayoutParams.WRAP_CONTENT)
                    text = value
                    gravity = Gravity.CENTER
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                }
                layoutStats.addView(tv)
            }
        }
    }
}

private fun View.dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density + 0.5f).toInt()
