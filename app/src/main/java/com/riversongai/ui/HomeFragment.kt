package com.riversongai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.riversongai.R
import com.riversongai.databinding.FragmentHomeBinding
import com.riversongai.ui.viewmodel.HomeViewModel
import com.riversongai.utils.UIStyleManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        applyUIStyle()
        observeViewModel()
        
        homeViewModel.loadAllData()
    }

    private fun setupUI() {
        val isAdmin = requireContext()
            .getSharedPreferences("rs_prefs", android.content.Context.MODE_PRIVATE)
            .getBoolean("is_admin", false)

        binding.textViewDate.text = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())

        binding.swipeRefresh.setOnRefreshListener {
            homeViewModel.loadAllData()
        }

        binding.cardActionSpeak.setOnClickListener {
            findNavController().navigate(R.id.speakFragment)
        }
        binding.cardActionChat.setOnClickListener {
            findNavController().navigate(R.id.chatFragment)
        }
        
        binding.cardActionHome.isVisible = isAdmin
        binding.cardActionHome.setOnClickListener {
            findNavController().navigate(R.id.smartHomeControlScreen)
        }
        binding.cardActionMemory.setOnClickListener {
            findNavController().navigate(R.id.memoryFragment)
        }
        
        binding.buttonViewAllRoutines.isVisible = isAdmin
        binding.buttonViewAllRoutines.setOnClickListener {
            findNavController().navigate(R.id.routinesFragment)
        }
    }

    private fun applyUIStyle() {
        val ctx = requireContext()
        // depth 1 = mid cards
        binding.cardGreeting.setCardBackgroundColor(UIStyleManager.resolveCardColor(ctx, 1))
        binding.cardRoutines.setCardBackgroundColor(UIStyleManager.resolveCardColor(ctx, 1))
        
        binding.cardStatsMemory.setCardBackgroundColor(UIStyleManager.resolveCardColor(ctx, 1))
        binding.cardStatsSummaries.setCardBackgroundColor(UIStyleManager.resolveCardColor(ctx, 1))
        binding.cardStatsUptime.setCardBackgroundColor(UIStyleManager.resolveCardColor(ctx, 1))
        binding.cardStatsLatency.setCardBackgroundColor(UIStyleManager.resolveCardColor(ctx, 1))

        // depth 2 = top cards (featured/prominent)
        binding.cardWeather.setCardBackgroundColor(UIStyleManager.resolveCardColor(ctx, 2))
        
        listOf(
            binding.cardActionSpeak, binding.cardActionChat,
            binding.cardActionHome, binding.cardActionMemory
        ).forEach {
            it.setCardBackgroundColor(UIStyleManager.resolveCardColor(ctx, 2))
        }
    }

    private fun observeViewModel() {
        homeViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.isVisible = loading
            binding.swipeRefresh.isRefreshing = loading
        }

        homeViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                val calendar = Calendar.getInstance()
                val greeting = when (calendar.get(Calendar.HOUR_OF_DAY)) {
                    in 0..11 -> "Good morning"
                    in 12..16 -> "Good afternoon"
                    else -> "Good evening"
                }
                binding.textViewGreeting.text = "$greeting, ${it.firstName}"
            }
        }

        homeViewModel.dashboard.observe(viewLifecycleOwner) { stats ->
            stats?.let {
                binding.textViewMemoryFacts.text = it.factsCount.toString()
                binding.textViewSummaries.text = it.summariesCount.toString()
                binding.textViewUptime.text = formatUptime(it.uptimeSeconds)
                binding.textViewLatency.text = "${it.avgLatencyMs.toInt()}ms"
                
                // Status dot (mocking "operational" as green)
                binding.viewStatusDot.setBackgroundResource(R.drawable.ic_check)
                val typedValue = android.util.TypedValue()
                requireContext().theme.resolveAttribute(com.google.android.material.R.attr.colorTertiary, typedValue, true)
                binding.viewStatusDot.backgroundTintList = android.content.res.ColorStateList.valueOf(typedValue.data)
                binding.textViewStatus.text = "River Song is online"
            }
        }

        homeViewModel.weather.observe(viewLifecycleOwner) { weather ->
            weather?.let {
                binding.textViewWeatherTemp.text = "%.0f°C".format(it.current.tempC)
                binding.textViewWeatherCondition.text = it.current.conditionText
                binding.textViewWeatherIcon.text = conditionToEmoji(it.current.conditionText)
            }
        }

        homeViewModel.routines.observe(viewLifecycleOwner) { routines ->
            binding.cardRoutines.isVisible = routines.isNotEmpty()
            binding.layoutRoutinesList.removeAllViews()
            routines.take(3).forEach { routine ->
                val tv = TextView(requireContext()).apply {
                    text = "${routine.name} • ${routine.scheduleTime ?: "Manual"}"
                    setPadding(0, 8, 0, 8)
                    textAppearance = com.google.android.material.R.style.TextAppearance_Material3_BodyMedium
                }
                binding.layoutRoutinesList.addView(tv)
            }
        }

        homeViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                homeViewModel.clearError()
            }
        }
        
        homeViewModel.sessionExpired.observe(viewLifecycleOwner) { expired ->
            if (expired == true) {
                findNavController().navigate(R.id.loginScreen, null, 
                    NavOptions.Builder().setPopUpTo(R.id.main_nav_graph, true).build())
            }
        }
    }

    private fun formatUptime(seconds: Long): String {
        val days = seconds / (24 * 3600)
        val hours = (seconds % (24 * 3600)) / 3600
        return "${days}d ${hours}h"
    }

    private fun conditionToEmoji(condition: String): String {
        return when {
            condition.contains("Sunny", true) || condition.contains("Clear", true) -> "☀️"
            condition.contains("Cloud", true) || condition.contains("Overcast", true) -> "☁️"
            condition.contains("Rain", true) || condition.contains("Drizzle", true) -> "🌧️"
            condition.contains("Storm", true) || condition.contains("Thunder", true) -> "⛈️"
            condition.contains("Snow", true) -> "❄️"
            else -> "⛅"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
