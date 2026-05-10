package com.riversongai.ui

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.riversongai.R
import com.riversongai.databinding.FragmentMemorySummariesBinding
import com.riversongai.ui.adapter.SummaryAdapter
import com.riversongai.ui.viewmodel.MemoryViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class MemorySummariesFragment : Fragment(R.layout.fragment_memory_summaries) {

    private var _binding: FragmentMemorySummariesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MemoryViewModel by activityViewModel()
    private lateinit var summaryAdapter: SummaryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMemorySummariesBinding.bind(view)

        summaryAdapter = SummaryAdapter { summary ->
            com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete this summary?")
                .setMessage("Are you sure you want to delete this session summary?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete") { _, _ -> viewModel.deleteSummary(summary.id) }
                .show()
        }
        binding.recyclerViewSummaries.apply {
            adapter = summaryAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.editTextSearch.addTextChangedListener {
            viewModel.setFilterQuery(it.toString())
        }

        viewModel.filteredSummaries.observe(viewLifecycleOwner) { summaries ->
            summaryAdapter.submitList(summaries)
            binding.textViewEmpty.visibility = if (summaries.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
