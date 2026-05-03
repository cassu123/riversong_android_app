package com.riversongai.ui

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.riversongai.R
import com.riversongai.databinding.FragmentFeedsWeatherBinding
import com.riversongai.databinding.ItemHourlyForecastBinding
import com.riversongai.ui.adapter.DailyForecastAdapter
import com.riversongai.ui.viewmodel.FeedsViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class WeatherFragment : Fragment(R.layout.fragment_feeds_weather) {

    private var _binding: FragmentFeedsWeatherBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FeedsViewModel by sharedViewModel()
    private lateinit var dailyAdapter: DailyForecastAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFeedsWeatherBinding.bind(view)

        dailyAdapter = DailyForecastAdapter()
        binding.recyclerViewDaily.apply {
            adapter = dailyAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.editTextLocation.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.saveWeatherLocation(v.text.toString())
                true
            } else false
        }

        binding.layoutLocation.setEndIconOnClickListener {
            // Simplified location detection
            viewModel.saveWeatherLocation("auto:ip")
        }

        viewModel.weather.observe(viewLifecycleOwner) { weather ->
            weather?.let {
                binding.editTextLocation.setText(it.location?.name ?: "", false)
                
                val current = it.current
                binding.textViewWeatherTemp.text = "%.0f°C".format(current.tempC)
                binding.textViewWeatherCondition.text = current.conditionText
                binding.textViewWeatherDetails.text = 
                    "Feels like %.0f°C  •  Humidity %d%%  •  Wind %.0f km/h".format(
                        current.feelsLikeC, current.humidity, current.windKph
                    )

                populateHourly(it.hourly)
                dailyAdapter.submitList(it.daily)
                populateAlerts(it.alerts)
            }
        }
    }

    private fun populateHourly(hourly: List<com.riversongai.data.model.HourlyForecast>) {
        binding.layoutHourly.removeAllViews()
        hourly.take(24).forEach { hour ->
            val itemBinding = ItemHourlyForecastBinding.inflate(layoutInflater, binding.layoutHourly, false)
            itemBinding.textViewHourlyTime.text = hour.time
            itemBinding.textViewHourlyIcon.text = getEmojiForCondition(hour.conditionText)
            itemBinding.textViewHourlyTemp.text = "%.0f°".format(hour.tempC)
            binding.layoutHourly.addView(itemBinding.root)
        }
    }

    private fun populateAlerts(alerts: List<com.riversongai.data.model.WeatherAlert>) {
        binding.cardWeatherAlerts.visibility = if (alerts.isEmpty()) View.GONE else View.VISIBLE
        binding.layoutAlerts.removeAllViews()
        alerts.firstOrNull()?.let {
            val tv = TextView(requireContext()).apply {
                text = "${it.headline}\n${it.description}"
                setPadding(0, 8, 0, 0)
            }
            binding.layoutAlerts.addView(tv)
        }
    }

    private fun getEmojiForCondition(text: String): String {
        return when {
            text.contains("sun", true) -> "☀️"
            text.contains("cloud", true) -> "☁️"
            text.contains("rain", true) -> "🌧️"
            text.contains("snow", true) -> "❄️"
            text.contains("storm", true) -> "⛈️"
            else -> "⛅"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
