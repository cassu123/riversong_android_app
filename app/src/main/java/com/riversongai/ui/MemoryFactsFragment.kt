package com.riversongai.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.riversongai.R
import com.riversongai.databinding.FragmentMemoryFactsBinding
import com.riversongai.ui.adapter.FactAdapter
import com.riversongai.ui.viewmodel.MemoryViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class MemoryFactsFragment : Fragment(R.layout.fragment_memory_facts) {

    private var _binding: FragmentMemoryFactsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MemoryViewModel by activityViewModel()
    private lateinit var factAdapter: FactAdapter

    private val selectedIds = mutableSetOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMemoryFactsBinding.bind(view)

        factAdapter = FactAdapter(
            onDelete = { fact -> viewModel.deleteFact(fact.id) },
            onSelect = { id, isSelected ->
                if (isSelected) selectedIds.add(id) else selectedIds.remove(id)
                updateToolbar()
            }
        )

        binding.recyclerViewFacts.apply {
            adapter = factAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.editTextSearch.addTextChangedListener {
            viewModel.setFilterQuery(it.toString())
        }

        binding.fabAddFact.setOnClickListener {
            (parentFragment as? MemoryFragment)?.showAddFactDialog()
        }

        binding.buttonForgetSelected.setOnClickListener {
            if (selectedIds.isNotEmpty()) {
                viewModel.deleteFacts(selectedIds)
                selectedIds.clear()
                updateToolbar()
            }
        }

        viewModel.filteredFacts.observe(viewLifecycleOwner) { facts ->
            factAdapter.submitList(facts)
            binding.textViewMemoryEmpty.visibility = if (facts.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.actionResult.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                viewModel.clearActionResult()
            }
        }
    }

    private fun updateToolbar() {
        binding.layoutBatchActions.isVisible = selectedIds.isNotEmpty()
        binding.buttonForgetSelected.text = "FORGET (${selectedIds.size})"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
