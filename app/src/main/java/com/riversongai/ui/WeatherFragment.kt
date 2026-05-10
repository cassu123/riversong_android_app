package com.riversongai.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.riversongai.R
import com.riversongai.databinding.FragmentFeedsWeatherBinding
import com.riversongai.databinding.ItemHourlyForecastBinding
import com.riversongai.ui.adapter.DailyForecastAdapter
import com.riversongai.ui.viewmodel.FeedsViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class WeatherFragment : Fragment() {

    private var _binding: FragmentFeedsWeatherBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FeedsViewModel by activityViewModel()
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

        binding.editTextLocation.apply {
            inputType = android.text.InputType.TYPE_NULL
            isFocusable = false
        }

        binding.layoutLocation.setEndIconOnClickListener {
            requestLocationUpdate()
        }

        binding.cardCurrentWeather.setOnClickListener {
            if (binding.textViewWeatherError.isVisible) {
                viewModel.loadWeather()
            }
        }

        binding.btnSetLocation.setOnClickListener {
            requestLocationUpdate()
        }

        setupSettingsToggles()
        observeViewModel()
    }

    private fun setupSettingsToggles() {
        binding.chipCelsius.setOnClickListener { viewModel.saveWeatherUnit("celsius") }
        binding.chipFahrenheit.setOnClickListener { viewModel.saveWeatherUnit("fahrenheit") }
    }

    private fun requestLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            return
        }

        val lm = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) 
            ?: lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        
        location?.let {
            viewModel.saveWeatherCoordinates(it.latitude, it.longitude)
        } ?: run {
            Snackbar.make(binding.root, "Could not determine location", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocationUpdate()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoadingWeather.observe(viewLifecycleOwner) { loading ->
            binding.progressWeather.isVisible = loading
            if (loading) {
                binding.layoutWeatherData.isVisible = false
                binding.textViewWeatherError.isVisible = false
                binding.layoutWeatherEmpty.isVisible = false
            }
        }

        viewModel.preferences.observe(viewLifecycleOwner) { prefs ->
            prefs?.let {
                binding.chipCelsius.isChecked = it.weatherUnit == "celsius"
                binding.chipFahrenheit.isChecked = it.weatherUnit == "fahrenheit"
            }
        }

        viewModel.weather.observe(viewLifecycleOwner) { weather ->
            if (viewModel.isLoadingWeather.value == true) return@observe

            if (weather == null) {
                binding.layoutWeatherData.isVisible = false
                binding.textViewWeatherError.isVisible = true
                binding.cardAirQuality.isVisible = false
                binding.layoutWeatherEmpty.isVisible = false
            } else if (weather.locationName.isBlank() && weather.lat == 0.0) {
                binding.layoutWeatherData.isVisible = false
                binding.textViewWeatherError.isVisible = false
                binding.cardAirQuality.isVisible = false
                binding.layoutWeatherEmpty.isVisible = true
            } else {
                binding.layoutWeatherData.isVisible = true
                binding.textViewWeatherError.isVisible = false
                binding.layoutWeatherEmpty.isVisible = false
                
                binding.editTextLocation.setText(weather.locationName)
                
                val current = weather.current
                val unit = current.unit.ifBlank { "°" }
                binding.textViewWeatherTemp.text = "%.0f%s".format(current.temperature, unit)
                binding.textViewWeatherCondition.text = current.condition
                binding.textViewWeatherIcon.text = conditionToIcon(current.condition)
                
                binding.textViewFeelsLike.text = "%.0f%s".format(current.feelsLike, unit)
                binding.textViewHumidity.text = "${current.humidity}%"
                binding.textViewWind.text = "%.0f km/h".format(current.windSpeed)
                
                binding.textViewVisibility.text = "%.0f km".format(current.visibility / 1000f)
                binding.textViewGusts.text = "%.0f km/h".format(current.windGusts)
                binding.textViewUvIndex.text = "%.1f".format(current.uvIndex)

                // Bind Air Quality
                val aq = weather.airQuality
                if (aq != null) {
                    binding.cardAirQuality.isVisible = true
                    binding.textViewAqi.text = "%.0f".format(aq.aqi)
                    binding.textViewAqiLabel.text = aq.label
                    try {
                        binding.cardAqiValue.setCardBackgroundColor(android.graphics.Color.parseColor(aq.color))
                    } catch (e: Exception) { /* ignore */ }
                    binding.textViewAqiPm25.text = "%.1f".format(aq.pm25)
                    binding.textViewAqiPm10.text = "%.1f".format(aq.pm10)
                    binding.textViewAqiOzone.text = "%.0f".format(aq.ozone)
                    binding.textViewAqiNo2.text = "%.1f".format(aq.nitrogenDioxide)
                    binding.textViewAqiCo.text = "%.0f".format(aq.carbonMonoxide)
                } else {
                    binding.cardAirQuality.isVisible = false
                }

                if (weather.hourly.isNotEmpty()) {
                    binding.scrollViewHourly.isVisible = true
                    populateHourly(weather.hourly, unit)
                } else {
                    binding.scrollViewHourly.isVisible = false
                }

                if (weather.daily.isNotEmpty()) {
                    binding.recyclerViewDaily.isVisible = true
                    dailyAdapter.submitList(weather.daily)
                } else {
                    binding.recyclerViewDaily.isVisible = false
                }
                
                populateAlerts(weather.alerts)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun populateHourly(hourly: List<com.riversongai.data.model.HourlyForecast>, unit: String) {
        binding.layoutHourly.removeAllViews()
        hourly.take(12).forEach { hour ->
            val itemBinding = ItemHourlyForecastBinding.inflate(layoutInflater, binding.layoutHourly, false)
            itemBinding.textViewHourlyTime.text = hour.time
            itemBinding.textViewHourlyIcon.text = conditionToIcon(hour.condition)
            itemBinding.textViewHourlyTemp.text = "%.0f%s".format(hour.temperature, unit)
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
                setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodySmall)
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
