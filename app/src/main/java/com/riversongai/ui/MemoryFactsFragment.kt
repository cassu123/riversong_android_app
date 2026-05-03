package com.riversongai.ui

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.riversongai.R
import com.riversongai.databinding.FragmentMemoryFactsBinding
import com.riversongai.ui.adapter.FactAdapter
import com.riversongai.ui.viewmodel.MemoryViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MemoryFactsFragment : Fragment(R.layout.fragment_memory_facts) {

    private var _binding: FragmentMemoryFactsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MemoryViewModel by sharedViewModel()
    private lateinit var factAdapter: FactAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMemoryFactsBinding.bind(view)

        factAdapter = FactAdapter(onDeleteClick = { fact ->
            viewModel.deleteFact(fact.id)
        })

        binding.recyclerViewFacts.apply {
            adapter = factAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.editTextSearch.addTextChangedListener {
            viewModel.setFilterQuery(it.toString())
        }

        binding.fabAddFact.setOnClickListener {
            // This might need to trigger a dialog in MemoryFragment or here
            (parentFragment as? MemoryFragment)?.showAddFactDialog()
        }

        viewModel.filteredFacts.observe(viewLifecycleOwner) { facts ->
            factAdapter.submitList(facts)
            binding.textViewMemoryEmpty.visibility = if (facts.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
