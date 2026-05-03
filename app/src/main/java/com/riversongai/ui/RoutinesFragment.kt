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
                Toast.makeText(context, "Running ${routine.name}…", Toast.LENGTH_SHORT).show()
                routinesViewModel.runRoutine(routine.id)
            },
            onDelete = { routine ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete routine")
                    .setMessage("Delete \"${routine.name}\"?")
                    .setPositiveButton("Delete") { _, _ -> routinesViewModel.deleteRoutine(routine.id) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        binding.recyclerViewRoutines.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = routineAdapter
        }

        routinesViewModel.routines.observe(viewLifecycleOwner) { routines ->
            routineAdapter.submitList(routines)
            binding.textViewRoutinesEmpty.isVisible = routines.isEmpty()
        }

        routinesViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBarRoutines.isVisible = loading
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

        binding.fabAddRoutine.setOnClickListener { showCreateRoutineDialog() }
    }

    private fun showCreateRoutineDialog() {
        val layout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 32, 48, 0)
        }
        val nameInput = EditText(context).apply { hint = "Routine name" }
        val promptInput = EditText(context).apply {
            hint = "Prompt (what should River Song do?)"
            minLines = 2
        }
        layout.addView(nameInput)
        layout.addView(promptInput)

        AlertDialog.Builder(requireContext())
            .setTitle("Create Routine")
            .setView(layout)
            .setPositiveButton("Create") { _, _ ->
                routinesViewModel.createRoutine(nameInput.text.toString(), promptInput.text.toString())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
