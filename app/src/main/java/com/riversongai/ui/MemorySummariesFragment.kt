package com.riversongai.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.riversongai.R
import com.riversongai.databinding.FragmentMemorySummariesBinding
import com.riversongai.ui.adapter.SummaryAdapter
import com.riversongai.ui.viewmodel.MemoryViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MemorySummariesFragment : Fragment(R.layout.fragment_memory_summaries) {

    private var _binding: FragmentMemorySummariesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MemoryViewModel by sharedViewModel()
    private lateinit var summaryAdapter: SummaryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMemorySummariesBinding.bind(view)

        summaryAdapter = SummaryAdapter()
        binding.recyclerViewSummaries.apply {
            adapter = summaryAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        viewModel.summaries.observe(viewLifecycleOwner) { summaries ->
            summaryAdapter.submitList(summaries)
            binding.textViewEmpty.visibility = if (summaries.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
