package com.riversongai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.riversongai.data.model.Routine
import com.riversongai.databinding.BottomSheetRoutineCreateEditBinding
import com.riversongai.ui.viewmodel.RoutinesViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class RoutineCreateEditBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetRoutineCreateEditBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RoutinesViewModel by sharedViewModel()

    private var selectedRoutine: Routine? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetRoutineCreateEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedRoutine = viewModel.editingRoutine.value
        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        selectedRoutine?.let { routine ->
            binding.textViewSheetTitle.text = "Edit Routine"
            binding.editTextRoutineName.setText(routine.name)
            binding.editTextRoutinePrompt.setText(routine.prompt)
            
            when (routine.triggerType) {
                "daily" -> binding.radioDaily.isChecked = true
                "weekly" -> binding.radioWeekly.isChecked = true
                "startup" -> binding.radioStartup.isChecked = true
                else -> binding.radioManual.isChecked = true
            }

            binding.editTextRoutineTime.setText(routine.scheduleTime)
            routine.scheduleDays?.forEach { day ->
                when (day) {
                    "Mon" -> binding.chipMon.isChecked = true
                    "Tue" -> binding.chipTue.isChecked = true
                    "Wed" -> binding.chipWed.isChecked = true
                    "Thu" -> binding.chipThu.isChecked = true
                    "Fri" -> binding.chipFri.isChecked = true
                    "Sat" -> binding.chipSat.isChecked = true
                    "Sun" -> binding.chipSun.isChecked = true
                }
            }
            updateVisibility(routine.triggerType)
        }
    }

    private fun setupListeners() {
        binding.radioGroupTrigger.setOnCheckedChangeListener { _, checkedId ->
            val type = when (checkedId) {
                binding.radioDaily.id -> "daily"
                binding.radioWeekly.id -> "weekly"
                binding.radioStartup.id -> "startup"
                else -> "manual"
            }
            updateVisibility(type)
        }

        binding.editTextRoutineTime.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setTitleText("Select Time")
                .build()
            picker.addOnPositiveButtonClickListener {
                val time = String.format("%02d:%02d", picker.hour, picker.minute)
                binding.editTextRoutineTime.setText(time)
            }
            picker.show(childFragmentManager, "time_picker")
        }

        binding.buttonSaveRoutine.setOnClickListener {
            saveRoutine()
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun updateVisibility(type: String) {
        binding.layoutRoutineTime.visibility = if (type == "daily" || type == "weekly") View.VISIBLE else View.GONE
        binding.layoutWeeklyDays.visibility = if (type == "weekly") View.VISIBLE else View.GONE
    }

    private fun saveRoutine() {
        val name = binding.editTextRoutineName.text.toString().trim()
        val prompt = binding.editTextRoutinePrompt.text.toString()
        val trigger = when (binding.radioGroupTrigger.checkedRadioButtonId) {
            binding.radioDaily.id -> "daily"
            binding.radioWeekly.id -> "weekly"
            binding.radioStartup.id -> "startup"
            else -> "manual"
        }
        val time = binding.editTextRoutineTime.text.toString().trim().takeIf { it.isNotBlank() }
        
        // a) Validation: Routine name
        if (name.isBlank()) {
            binding.layoutRoutineName.error = "Name is required"
            return
        } else {
            binding.layoutRoutineName.error = null
        }

        // b & c) Validation for recurring triggers
        if (trigger == "daily" || trigger == "weekly") {
            if (time == null) {
                binding.layoutRoutineTime.error = "Time is required"
                return
            } else {
                binding.layoutRoutineTime.error = null
            }
        }

        val days = if (trigger == "weekly") {
            val list = mutableListOf<String>()
            if (binding.chipMon.isChecked) list.add("Mon")
            if (binding.chipTue.isChecked) list.add("Tue")
            if (binding.chipWed.isChecked) list.add("Wed")
            if (binding.chipThu.isChecked) list.add("Thu")
            if (binding.chipFri.isChecked) list.add("Fri")
            if (binding.chipSat.isChecked) list.add("Sat")
            if (binding.chipSun.isChecked) list.add("Sun")
            
            // b) Validation: Weekly trigger days
            if (list.isEmpty()) {
                com.google.android.material.snackbar.Snackbar.make(binding.root, "Please select at least one day", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
                return
            }
            list
        } else null

        if (selectedRoutine == null) {
            viewModel.createRoutine(name, prompt, trigger, time, days)
        } else {
            viewModel.updateRoutine(selectedRoutine!!.id, name, prompt, trigger, time, days, selectedRoutine!!.isEnabled)
        }
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "RoutineCreateEditBottomSheet"
    }
}
