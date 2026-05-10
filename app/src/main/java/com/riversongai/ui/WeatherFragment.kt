package com.riversongai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.riversongai.R
import com.riversongai.databinding.FragmentFeedsWeatherBinding
import com.riversongai.databinding.ItemHourlyForecastBinding
import com.riversongai.ui.adapter.DailyForecastAdapter
import com.riversongai.ui.viewmodel.FeedsViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class WeatherFragment : Fragment() {

    private var _binding: FragmentFeedsWeatherBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FeedsViewModel by sharedViewModel()
    private lateinit var dailyAdapter: DailyForecastAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFeedsWeatherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            viewModel.saveWeatherLocation("auto:ip")
        }

        viewModel.weather.observe(viewLifecycleOwner) { weather ->
            weather?.let {
                binding.editTextLocation.setText(it.location?.name ?: "")
                
                val current = it.current
                binding.textViewWeatherTemp.text = "%.0f°".format(current.tempC)
                binding.textViewWeatherCondition.text = current.conditionText
                binding.textViewWeatherIcon.text = conditionToIcon(current.conditionText)
                
                binding.textViewFeelsLike.text = "%.0f°".format(current.feelsLikeC)
                binding.textViewHumidity.text = "${current.humidity}%"
                binding.textViewWind.text = "%.0f km/h".format(current.windKph)

                if (it.hourly.isNotEmpty()) {
                    binding.scrollViewHourly.isVisible = true
                    populateHourly(it.hourly)
                } else {
                    binding.scrollViewHourly.isVisible = false
                }

                if (it.daily.isNotEmpty()) {
                    binding.recyclerViewDaily.isVisible = true
                    dailyAdapter.submitList(it.daily)
                } else {
                    binding.recyclerViewDaily.isVisible = false
                }
                
                populateAlerts(it.alerts)
            }
        }
    }

    private fun populateHourly(hourly: List<com.riversongai.data.model.HourlyForecast>) {
        binding.layoutHourly.removeAllViews()
        hourly.take(12).forEach { hour ->
            val itemBinding = ItemHourlyForecastBinding.inflate(layoutInflater, binding.layoutHourly, false)
            itemBinding.textViewHourlyTime.text = hour.time
            itemBinding.textViewHourlyIcon.text = conditionToIcon(hour.conditionText)
            itemBinding.textViewHourlyTemp.text = "%.0f°".format(hour.tempC)
            binding.layoutHourly.addView(itemBinding.root)
        }
    }

    private fun populateAlerts(alerts: List<com.riversongai.data.model.WeatherAlert>) {
        binding.cardWeatherAlerts.isVisible = alerts.isNotEmpty()
        binding.layoutAlerts.removeAllViews()
        alerts.firstOrNull()?.let {
            val tv = TextView(requireContext()).apply {
                text = "${it.headline}\n${it.description}"
                setPadding(0, 8, 0, 0)
                textAppearance = com.google.android.material.R.style.TextAppearance_Material3_BodySmall
            }
            binding.layoutAlerts.addView(tv)
        }
    }

    private fun conditionToIcon(condition: String): String {
        val c = condition.lowercase()
        return when {
            c.contains("sunny") || c.contains("clear") -> "☀️"
            c.contains("storm") || c.contains("thunder") -> "⛈️"
            c.contains("rain") || c.contains("drizzle") -> "🌧️"
            c.contains("snow") -> "❄️"
            c.contains("cloud") || c.contains("overcast") -> "☁️"
            c.contains("wind") -> "🌬️"
            else -> "⛅"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
