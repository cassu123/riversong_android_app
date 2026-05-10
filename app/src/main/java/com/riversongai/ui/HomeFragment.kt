package com.riversongai.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.riversongai.R
import com.riversongai.databinding.FragmentHomeBinding
import com.riversongai.ui.viewmodel.HomeViewModel
import com.riversongai.utils.UIStyleManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by viewModel()

    private var isArrangeMode = false

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
        
        homeViewModel.loadWidgetVisibility(requireContext())
        homeViewModel.loadAllData()
    }

    private fun setupUI() {
        val isAdmin = requireContext()
            .getSharedPreferences("rs_prefs", Context.MODE_PRIVATE)
            .getBoolean("is_admin", false)

        binding.btnQuickListen.setOnClickListener { findNavController().navigate(R.id.speakFragment) }
        binding.btnQuickRoutine.setOnClickListener { findNavController().navigate(R.id.routinesFragment) }
        binding.btnQuickHomeScene.setOnClickListener { findNavController().navigate(R.id.smartHomeControlScreen) }
        binding.btnQuickLogEvent.setOnClickListener { findNavController().navigate(R.id.memoryFragment) }

        binding.textViewDate.text = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())

        binding.swipeRefresh.setOnRefreshListener {
            homeViewModel.loadAllData()
        }

        binding.buttonViewAllRoutines.setOnClickListener {
            findNavController().navigate(R.id.routinesFragment)
        }

        binding.btnArrange.setOnClickListener {
            isArrangeMode = !isArrangeMode
            binding.btnArrange.text = if (isArrangeMode) "DONE" else "ARRANGE"
            binding.chipGroupWidgets.isVisible = isArrangeMode
            if (!isArrangeMode) homeViewModel.loadAllData()
        }
    }

    private fun applyUIStyle() {
        val ctx = requireContext()
        binding.cardSystemStatus.setCardBackgroundColor(UIStyleManager.resolveCardColor(ctx, 1))
        binding.cardMemoryActivity.setCardBackgroundColor(UIStyleManager.resolveCardColor(ctx, 1))
        binding.cardRecentSessions.setCardBackgroundColor(UIStyleManager.resolveCardColor(ctx, 1))
        binding.cardRoutines.setCardBackgroundColor(UIStyleManager.resolveCardColor(ctx, 1))
        binding.cardWeather.setCardBackgroundColor(UIStyleManager.resolveCardColor(ctx, 2))
    }

    private fun observeViewModel() {
        homeViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.isVisible = loading
            binding.swipeRefresh.isRefreshing = loading
        }

        homeViewModel.widgetVisibility.observe(viewLifecycleOwner) { visibility ->
            updateWidgetVisibility(visibility)
            if (isArrangeMode) populateArrangeChips(visibility)
        }

        homeViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val greet = when { h < 12 -> "Good morning"; h < 18 -> "Good afternoon"; else -> "Good evening" }
                binding.textViewGreeting.text = "$greet, ${it.firstName}"
            }
        }

        homeViewModel.dashboard.observe(viewLifecycleOwner) { stats ->
            stats?.let {
                binding.textViewLatency.text = "${it.latencyMs}ms"
                binding.textViewUptime.text = it.uptime
                binding.textViewMemoryFacts.text = it.memory.facts.toString()
                binding.textViewMemorySummaries.text = it.memory.summaries.toString()
                
                binding.textViewStatus.text = "RIVER IS ${it.status.uppercase()}"
                val color = if (it.status == "operational") com.google.android.material.R.attr.colorTertiary else com.google.android.material.R.attr.colorError
                binding.viewStatusDot.backgroundTintList = android.content.res.ColorStateList.valueOf(UIStyleManager.resolveAttrColor(requireContext(), color))
                
                drawMemoryBars(it.memory.facts + it.memory.summaries)
            }
        }

        homeViewModel.weather.observe(viewLifecycleOwner) { weather ->
            weather?.let {
                val unit = it.current.unit.ifBlank { "°" }
                binding.textViewWeatherTemp.text = "%.0f%s".format(it.current.temperature, unit)
                binding.textViewWeatherCondition.text = it.current.condition
                binding.textViewWeatherIcon.text = conditionToEmoji(it.current.condition)
            }
        }

        homeViewModel.routines.observe(viewLifecycleOwner) { routines ->
            binding.layoutRoutinesList.removeAllViews()
            routines.take(3).forEach { r ->
                val tv = TextView(requireContext()).apply {
                    text = "${r.name} • ${if (r.isEnabled) "ON" else "OFF"}"
                    setPadding(0, 4, 0, 4)
                    setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodySmall)
                }
                binding.layoutRoutinesList.addView(tv)
            }
        }

        homeViewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            msg?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show(); homeViewModel.clearError() }
        }
        
        homeViewModel.sessionExpired.observe(viewLifecycleOwner) { expired ->
            if (expired == true) {
                findNavController().navigate(R.id.loginScreen, null, 
                    NavOptions.Builder().setPopUpTo(R.id.main_nav_graph, true).build())
            }
        }
    }

    private fun updateWidgetVisibility(visibility: Map<String, Boolean>) {
        binding.cardSystemStatus.isVisible = visibility["system_status"] ?: true
        binding.layoutQuickActions.isVisible = visibility["quick_actions"] ?: true
        binding.cardMemoryActivity.isVisible = visibility["memory_activity"] ?: true
        binding.cardWeather.isVisible = visibility["weather"] ?: true
        binding.cardRecentSessions.isVisible = visibility["recent_sessions"] ?: true
        binding.cardRoutines.isVisible = visibility["active_routines"] ?: true
    }

    private fun populateArrangeChips(visibility: Map<String, Boolean>) {
        binding.chipGroupWidgets.removeAllViews()
        visibility.forEach { (key, isVisible) ->
            val chip = Chip(requireContext()).apply {
                text = key.replace("_", " ").uppercase()
                isCheckable = true
                isChecked = isVisible
                setOnClickListener { homeViewModel.toggleWidget(requireContext(), key) }
            }
            binding.chipGroupWidgets.addView(chip)
        }
    }

    private fun drawMemoryBars(total: Int) {
        binding.layoutMemoryBars.removeAllViews()
        val bars = 30
        for (i in 0 until bars) {
            val bar = View(requireContext()).apply {
                val height = if (total > 0) {
                    (8 + (total.toFloat() / bars) * (0.5 + sin(i * 1.3 + 1) * 0.5)).coerceIn(6.0, 48.0).toInt()
                } else (8 + sin(i * 0.7) * 4).toInt()
                
                layoutParams = LinearLayout.LayoutParams(0, (height * resources.displayMetrics.density).toInt(), 1f).apply {
                    marginEnd = (2 * resources.displayMetrics.density).toInt()
                }
                setBackgroundColor(UIStyleManager.resolveAttrColor(requireContext(), com.google.android.material.R.attr.colorPrimary))
                alpha = if (i % 2 == 0) 0.8f else 0.4f
            }
            binding.layoutMemoryBars.addView(bar)
        }
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
