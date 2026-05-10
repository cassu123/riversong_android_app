package com.riversongai.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.riversongai.data.model.DailyForecast
import com.riversongai.databinding.ItemDailyForecastBinding

class DailyForecastAdapter : ListAdapter<DailyForecast, DailyForecastAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDailyForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemDailyForecastBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(forecast: DailyForecast) {
            binding.textViewDailyDay.text = forecast.date
            binding.textViewDailyIcon.text = getEmojiForCondition(forecast.condition)
            binding.textViewDailyPrecip.text = "%.1fmm".format(forecast.precipitation)
            binding.textViewDailyTempRange.text = "%.0f° / %.0f°".format(forecast.tempMax, forecast.tempMin)
            
            if (forecast.sunrise.isNotBlank() && forecast.sunset.isNotBlank()) {
                binding.textViewSunriseSunset.isVisible = true
                binding.textViewSunriseSunset.text = "🌅 ${forecast.sunrise}  🌇 ${forecast.sunset}"
            } else {
                binding.textViewSunriseSunset.isVisible = false
            }
        }
    }

    private fun getEmojiForCondition(text: String?): String {
        val t = text ?: ""
        return when {
            t.contains("sun", true) || t.contains("clear", true) -> "☀️"
            t.contains("cloud", true) || t.contains("overcast", true) -> "☁️"
            t.contains("rain", true) || t.contains("drizzle", true) -> "🌧️"
            t.contains("snow", true) -> "❄️"
            t.contains("storm", true) || t.contains("thunder", true) -> "⛈️"
            else -> "⛅"
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<DailyForecast>() {
        override fun areItemsTheSame(oldItem: DailyForecast, newItem: DailyForecast) = oldItem.date == newItem.date
        override fun areContentsTheSame(oldItem: DailyForecast, newItem: DailyForecast) = oldItem == newItem
    }
}
