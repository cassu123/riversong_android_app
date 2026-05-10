package com.riversongai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.riversongai.R
import com.riversongai.data.model.Routine
import com.riversongai.databinding.FragmentRoutinesBinding
import com.riversongai.databinding.ItemRoutineBinding
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
            onToggle = { viewModel.toggleRoutine(it) },
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
        val sheet = RoutineCreateEditBottomSheet()
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

    private inner class RoutineAdapter(
        private val onRun: (Routine) -> Unit,
        private val onToggle: (Routine) -> Unit,
        private val onEdit: (Routine) -> Unit,
        private val onDelete: (Routine) -> Unit
    ) : ListAdapter<Routine, RoutineAdapter.VH>(DIFF) {

        inner class VH(val b: ItemRoutineBinding) : RecyclerView.ViewHolder(b.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(ItemRoutineBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun onBindViewHolder(holder: VH, position: Int) {
            val r = getItem(position)
            holder.b.textViewRoutineName.text = r.name
            holder.b.chipRoutineSchedule.text = formatSchedule(r)
            holder.b.textViewPromptPreview.text = r.prompt
            holder.b.textViewLastRun.text = if (r.lastRun != null) "Last run: ${r.lastRun}" else "Never run"
            
            holder.b.switchRoutineEnabled.isChecked = r.isEnabled
            holder.b.switchRoutineEnabled.setOnCheckedChangeListener { _, _ -> onToggle(r) }
            
            holder.b.buttonRunRoutine.setOnClickListener { onRun(r) }
            holder.b.buttonEditRoutine.setOnClickListener { onEdit(r) }
            holder.b.buttonDeleteRoutine.setOnClickListener { onDelete(r) }
            
            holder.b.root.alpha = if (r.isEnabled) 1.0f else 0.6f
        }

        private fun formatSchedule(r: Routine): String {
            return when (r.trigger) {
                "daily" -> "Daily at ${r.time ?: "--:--"}"
                "weekly" -> "Weekly: ${r.days.joinToString("/")}"
                "startup" -> "On Startup"
                else -> "Manual"
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Routine>() {
            override fun areItemsTheSame(a: Routine, b: Routine) = a.id == b.id
            override fun areContentsTheSame(a: Routine, b: Routine) = a == b
        }
    }
}
