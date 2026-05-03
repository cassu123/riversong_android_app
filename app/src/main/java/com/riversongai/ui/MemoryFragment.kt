package com.riversongai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.riversongai.R
import com.riversongai.databinding.FragmentMemoryBinding
import com.riversongai.ui.adapter.FactAdapter
import com.riversongai.ui.viewmodel.MemoryViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

import com.google.android.material.tabs.TabLayoutMediator
import com.riversongai.ui.adapter.MemoryPagerAdapter

class MemoryFragment : Fragment() {

    private var _binding: FragmentMemoryBinding? = null
    private val binding get() = _binding!!

    private val memoryViewModel: MemoryViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMemoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        setupRefresh()
        observeViewModel()
    }

    private fun setupViewPager() {
        binding.viewPagerMemory.adapter = MemoryPagerAdapter(this)
        TabLayoutMediator(binding.tabLayoutMemory, binding.viewPagerMemory) { tab, position ->
            tab.text = when (position) {
                0 -> "Facts"
                1 -> "Preferences"
                2 -> "Summaries"
                else -> ""
            }
        }.attach()
    }

    private fun setupRefresh() {
        binding.swipeRefreshMemory.setOnRefreshListener {
            when (binding.tabLayoutMemory.selectedTabPosition) {
                0 -> memoryViewModel.loadFacts()
                1 -> memoryViewModel.loadPreferences()
                2 -> memoryViewModel.loadSummaries()
            }
        }
    }

    private fun observeViewModel() {
        memoryViewModel.memoryStats.observe(viewLifecycleOwner) { stats ->
            binding.textStatFacts.text = "${stats.factsCount} Facts"
            binding.textStatPrefs.text = "${stats.prefsCount} Preferences"
            binding.textStatSessions.text = "${stats.sessionsCount} Summaries"
        }

        memoryViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.swipeRefreshMemory.isRefreshing = loading
        }

        memoryViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                memoryViewModel.clearError()
            }
        }

        memoryViewModel.actionResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                memoryViewModel.clearActionResult()
            }
        }
    }

    fun showAddFactDialog() {
        val layout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 32, 48, 0)
        }
        val keyInput = EditText(context).apply { hint = "Fact key (e.g. Favorite Color)" }
        val valueInput = EditText(context).apply { hint = "Fact value (e.g. Blue)" }
        layout.addView(keyInput)
        layout.addView(valueInput)
        AlertDialog.Builder(requireContext())
            .setTitle("Add Memory Fact")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                memoryViewModel.addFact(keyInput.text.toString(), valueInput.text.toString())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
