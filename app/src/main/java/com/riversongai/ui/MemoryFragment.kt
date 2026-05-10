package com.riversongai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayoutMediator
import com.riversongai.R
import com.riversongai.databinding.FragmentMemoryBinding
import com.riversongai.ui.adapter.MemoryPagerAdapter
import com.riversongai.ui.viewmodel.MemoryViewModel
import com.riversongai.utils.UIStyleManager
import org.koin.androidx.viewmodel.ext.android.viewModel

class MemoryFragment : Fragment(R.layout.fragment_memory) {

    private var _binding: FragmentMemoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MemoryViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMemoryBinding.bind(view)

        setupViewPager()
        setupListeners()
        applyUIStyle()
        observeViewModel()
        
        viewModel.loadStats()
    }

    private fun setupViewPager() {
        binding.viewPagerMemory.adapter = MemoryPagerAdapter(this)
        TabLayoutMediator(binding.tabLayoutMemory, binding.viewPagerMemory) { tab, position ->
            tab.text = when (position) {
                0 -> "Facts"
                1 -> "Preferences"
                else -> "Summaries"
            }
        }.attach()
    }

    private fun setupListeners() {
        binding.swipeRefreshMemory.setOnRefreshListener {
            viewModel.loadStats()
            // Also need to refresh the current fragment in viewpager? 
            // The sub-fragments observe the same VM, so just reloading stats might not be enough for their lists.
            // But usually they load on their own onViewCreated.
        }
    }

    private fun applyUIStyle() {
        val ctx = requireContext()
        binding.cardStatFacts.setCardBackgroundColor(UIStyleManager.resolveCardColor(ctx, 1))
        binding.cardStatPrefs.setCardBackgroundColor(UIStyleManager.resolveCardColor(ctx, 1))
        binding.cardStatSessions.setCardBackgroundColor(UIStyleManager.resolveCardColor(ctx, 1))
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.progressBarMemory.visibility = if (it) View.VISIBLE else View.GONE
            binding.swipeRefreshMemory.isRefreshing = it
        }

        viewModel.memoryStats.observe(viewLifecycleOwner) { stats ->
            stats?.let {
                binding.textStatFacts.text = "${it.factsCount} Facts"
                binding.textStatPrefs.text = "${it.prefsCount} Preferences"
                binding.textStatSessions.text = "${it.sessionsCount} Summaries"
            }
        }
    }

    fun showAddFactDialog() {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val padding = (24 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
        }

        val etKey = EditText(context).apply {
            hint = "Fact (e.g. Favorite Food)"
        }
        val etValue = EditText(context).apply {
            hint = "Value (e.g. Pizza)"
        }

        layout.addView(etKey)
        layout.addView(etValue)

        com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
            .setTitle("Add Memory Fact")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val key = etKey.text.toString()
                val value = etValue.text.toString()
                if (key.isNotBlank() && value.isNotBlank()) {
                    viewModel.addFact(key, value)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
