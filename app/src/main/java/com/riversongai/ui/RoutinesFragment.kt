package com.riversongai.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.riversongai.R
import com.riversongai.data.model.Routine
import com.riversongai.databinding.FragmentRoutinesBinding
import com.riversongai.ui.adapter.RoutineAdapter
import com.riversongai.ui.viewmodel.RoutinesViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class RoutinesFragment : Fragment(R.layout.fragment_routines) {

    private var _binding: FragmentRoutinesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RoutinesViewModel by viewModel()
    private lateinit var adapter: RoutineAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRoutinesBinding.bind(view)

        adapter = RoutineAdapter(
            onRun = { viewModel.runRoutine(it) },
            onToggle = { routine, _ -> viewModel.toggleRoutine(routine) },
            onEdit = { showEditRoutine(it) },
            onDelete = { showDeleteConfirm(it) }
        )

        binding.recyclerViewRoutines.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@RoutinesFragment.adapter
        }

        binding.swipeRefreshRoutines.setOnRefreshListener {
            viewModel.loadRoutines()
            viewModel.loadN8nStatus()
        }

        binding.fabAddRoutine.setOnClickListener { showAddRoutine() }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.swipeRefreshRoutines.isRefreshing = it
        }

        viewModel.routines.observe(viewLifecycleOwner) { routines ->
            adapter.submitList(routines)
            binding.textViewRoutinesEmpty.isVisible = routines.isEmpty()
        }

        viewModel.n8nStatus.observe(viewLifecycleOwner) { status ->
            val online = status["n8n_available"] == true
            binding.chipN8nStatus.text = if (online) "ONLINE" else "OFFLINE"
            binding.textViewN8nStatus.text = if (online) 
                "n8n is online and ready for complex multi-step automations." 
                else "n8n instance not detected. Advanced routines are unavailable."
        }

        viewModel.routineOutput.observe(viewLifecycleOwner) { output ->
            output?.let { (name, text) ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("$name Output")
                    .setMessage(text)
                    .setPositiveButton("Close") { _, _ -> viewModel.clearOutput() }
                    .setOnDismissListener { viewModel.clearOutput() }
                    .show()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { err ->
            err?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show(); viewModel.clearError() }
        }
    }

    private fun showAddRoutine() {
        val sheet = RoutineCreateEditBottomSheet.newInstance()
        sheet.show(childFragmentManager, "add_routine")
    }

    private fun showEditRoutine(routine: Routine) {
        val sheet = RoutineCreateEditBottomSheet.newInstance(routine)
        sheet.show(childFragmentManager, "edit_routine")
    }

    private fun showDeleteConfirm(routine: Routine) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Routine?")
            .setMessage("Are you sure you want to delete \"${routine.name}\"?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ -> viewModel.deleteRoutine(routine.id) }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
