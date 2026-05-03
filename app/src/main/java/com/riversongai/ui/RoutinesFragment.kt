package com.riversongai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.riversongai.R
import com.riversongai.databinding.FragmentRoutinesBinding
import com.riversongai.ui.adapter.RoutineAdapter
import com.riversongai.ui.viewmodel.RoutinesViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class RoutinesFragment : Fragment() {

    private var _binding: FragmentRoutinesBinding? = null
    private val binding get() = _binding!!

    private val routinesViewModel: RoutinesViewModel by viewModel()
    private lateinit var routineAdapter: RoutineAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRoutinesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        routineAdapter = RoutineAdapter(
            onToggle = { routine, enabled -> routinesViewModel.toggleRoutine(routine.id, enabled) },
            onRun = { routine ->
                Toast.makeText(context, getString(R.string.routines_running_prefix, routine.name), Toast.LENGTH_SHORT).show()
                routinesViewModel.runRoutine(routine.id)
            },
            onDelete = { routine ->
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.routines_delete_confirm_title)
                    .setMessage(getString(R.string.routines_delete_confirm_message, routine.name))
                    .setPositiveButton(android.R.string.ok) { _, _ -> routinesViewModel.deleteRoutine(routine.id) }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
        )

        binding.recyclerViewRoutines.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = routineAdapter
        }

        binding.swipeRefreshRoutines.apply {
            val typedValue = android.util.TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
            setColorSchemeColors(typedValue.data)
            setOnRefreshListener { routinesViewModel.loadRoutines() }
        }

        routinesViewModel.routines.observe(viewLifecycleOwner) { routines ->
            routineAdapter.submitList(routines)
            binding.textViewRoutinesEmpty.isVisible = routines.isEmpty()
        }

        routinesViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.swipeRefreshRoutines.isRefreshing = loading
            binding.progressBarRoutines.isVisible = loading && !binding.swipeRefreshRoutines.isRefreshing
        }

        routinesViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                routinesViewModel.clearError()
            }
        }

        routinesViewModel.actionResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                routinesViewModel.clearActionResult()
            }
        }

        routinesViewModel.routineRunOutput.observe(viewLifecycleOwner) { output ->
            output?.let {
                showRunOutputDialog(it)
                routinesViewModel.clearRoutineRunOutput()
            }
        }

        binding.fabAddRoutine.setOnClickListener { showCreateRoutineDialog() }
    }

    private fun showRunOutputDialog(output: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.routines_output_title)
            .setMessage(output)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun showCreateRoutineDialog() {
        val layout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 32, 48, 0)
        }
        val nameInput = EditText(context).apply { hint = getString(R.string.routines_name_hint) }
        val promptInput = EditText(context).apply {
            hint = getString(R.string.routines_prompt_hint)
            minLines = 2
        }
        layout.addView(nameInput)
        layout.addView(promptInput)

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.routines_create)
            .setView(layout)
            .setPositiveButton(R.string.memory_save) { _, _ ->
                routinesViewModel.createRoutine(nameInput.text.toString(), promptInput.text.toString())
            }
            .setNegativeButton(R.string.memory_cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
