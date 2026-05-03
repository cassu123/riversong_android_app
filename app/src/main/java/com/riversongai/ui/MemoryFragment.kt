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
import com.riversongai.databinding.FragmentMemoryBinding
import com.riversongai.ui.adapter.FactAdapter
import com.riversongai.ui.viewmodel.MemoryViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MemoryFragment : Fragment() {

    private var _binding: FragmentMemoryBinding? = null
    private val binding get() = _binding!!

    private val memoryViewModel: MemoryViewModel by viewModel()
    private lateinit var factAdapter: FactAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMemoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        factAdapter = FactAdapter(onDelete = { fact ->
            AlertDialog.Builder(requireContext())
                .setTitle("Delete fact")
                .setMessage("Delete \"${fact.key}\"?")
                .setPositiveButton("Delete") { _, _ -> memoryViewModel.deleteFact(fact.id) }
                .setNegativeButton("Cancel", null)
                .show()
        })

        binding.recyclerViewFacts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = factAdapter
        }

        binding.swipeRefreshMemory.apply {
            val typedValue = android.util.TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
            setColorSchemeColors(typedValue.data)
            setOnRefreshListener { memoryViewModel.loadFacts() }
        }

        binding.editTextSearch.doAfterTextChanged { text ->
            memoryViewModel.setFilterQuery(text?.toString().orEmpty())
        }

        memoryViewModel.filteredFacts.observe(viewLifecycleOwner) { facts ->
            factAdapter.submitList(facts)
            binding.textViewMemoryEmpty.isVisible = facts.isEmpty()
        }

        memoryViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.swipeRefreshMemory.isRefreshing = loading
            // Hide progress bar if we have SwipeRefresh
            binding.progressBarMemory.isVisible = loading && !binding.swipeRefreshMemory.isRefreshing
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

        binding.fabAddFact.setOnClickListener { showAddFactDialog() }
    }

    private fun showAddFactDialog() {
        val layout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 32, 48, 0)
        }
        val keyInput = EditText(context).apply { hint = "Key (e.g. favorite_color)" }
        val valueInput = EditText(context).apply { hint = "Value (e.g. purple)" }
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
