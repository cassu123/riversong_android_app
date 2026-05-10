package com.riversongai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.riversongai.data.model.Routine
import com.riversongai.data.model.RoutineCreate
import com.riversongai.databinding.BottomSheetRoutineCreateEditBinding
import com.riversongai.ui.viewmodel.RoutinesViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class RoutineCreateEditBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetRoutineCreateEditBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RoutinesViewModel by activityViewModel()
    
    private var routineToEdit: Routine? = null
    private val selectedDays = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        routineToEdit = arguments?.getSerializable(ARG_ROUTINE) as? Routine
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetRoutineCreateEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupTriggers()
        setupDays()

        routineToEdit?.let { r ->
            binding.textViewTitle.text = "Edit Routine"
            binding.editTextName.setText(r.name)
            binding.editTextPrompt.setText(r.prompt)
            binding.editTextTime.setText(r.time)
            
            // Set trigger selection
            val triggerIdx = listOf("daily", "weekly", "startup", "manual").indexOf(r.trigger)
            if (triggerIdx != -1) binding.spinnerTrigger.setSelection(triggerIdx)
            
            r.days.forEach { day ->
                selectedDays.add(day)
                updateDayChip(day, true)
            }
        }

        binding.buttonSave.setOnClickListener { save() }
        binding.buttonCancel.setOnClickListener { dismiss() }
    }

    private fun setupTriggers() {
        val triggers = listOf("Daily", "Weekly", "Startup", "Manual")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, triggers)
        binding.spinnerTrigger.adapter = adapter
    }

    private fun setupDays() {
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        binding.chipGroupDays.removeAllViews()
        days.forEach { day ->
            val chip = Chip(requireContext()).apply {
                text = day
                isCheckable = true
                isChecked = selectedDays.contains(day)
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) selectedDays.add(day) else selectedDays.remove(day)
                }
            }
            binding.chipGroupDays.addView(chip)
        }
    }

    private fun updateDayChip(day: String, isChecked: Boolean) {
        for (i in 0 until binding.chipGroupDays.childCount) {
            val chip = binding.chipGroupDays.getChildAt(i) as Chip
            if (chip.text == day) chip.isChecked = isChecked
        }
    }

    private fun save() {
        val name = binding.editTextName.text.toString().trim()
        if (name.isBlank()) { binding.layoutName.error = "Required"; return }
        
        val trigger = binding.spinnerTrigger.selectedItem.toString().lowercase()
        val time = binding.editTextTime.text.toString().takeIf { it.isNotBlank() }
        val prompt = binding.editTextPrompt.text.toString().trim()

        if (routineToEdit == null) {
            viewModel.createRoutine(RoutineCreate(
                name = name,
                trigger = trigger,
                time = time,
                days = selectedDays.toList(),
                prompt = prompt,
                isEnabled = true
            ))
        } else {
            viewModel.updateRoutine(routineToEdit!!.id, mapOf(
                "name" to name,
                "trigger" to trigger,
                "time" to time,
                "days" to selectedDays.toList(),
                "prompt" to prompt
            ))
        }
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_ROUTINE = "routine"
        fun newInstance(routine: Routine? = null) = RoutineCreateEditBottomSheet().apply {
            arguments = Bundle().apply { putSerializable(ARG_ROUTINE, routine) }
        }
    }
}
