package com.riversongai.ui

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.riversongai.R
import com.riversongai.data.model.Routine
import com.riversongai.data.model.RoutineCreate
import com.riversongai.databinding.BottomSheetRoutineCreateEditBinding
import com.riversongai.ui.viewmodel.RoutinesViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Calendar

class RoutineCreateEditBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetRoutineCreateEditBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RoutinesViewModel by viewModel()
    private var routineToEdit: Routine? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetRoutineCreateEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        routineToEdit = arguments?.getSerializable("routine") as? Routine
        
        if (routineToEdit != null) {
            binding.textViewSheetTitle.text = "Edit Routine"
            binding.editTextRoutineName.setText(routineToEdit?.name)
            binding.editTextRoutinePrompt.setText(routineToEdit?.prompt)
            binding.editTextRoutineTime.setText(routineToEdit?.time)
            
            when (routineToEdit?.trigger) {
                "daily" -> binding.radioDaily.isChecked = true
                "weekly" -> binding.radioWeekly.isChecked = true
                "startup" -> binding.radioStartup.isChecked = true
                else -> binding.radioManual.isChecked = true
            }
            updateTriggerVisibility(routineToEdit?.trigger ?: "manual")
        }

        binding.radioGroupTrigger.setOnCheckedChangeListener { _, checkedId ->
            val trigger = when (checkedId) {
                R.id.radioDaily -> "daily"
                R.id.radioWeekly -> "weekly"
                R.id.radioStartup -> "startup"
                else -> "manual"
            }
            updateTriggerVisibility(trigger)
        }

        binding.editTextRoutineTime.setOnClickListener { showTimePicker() }
        
        binding.buttonSaveRoutine.setOnClickListener { saveRoutine() }
        binding.buttonCancel.setOnClickListener { dismiss() }
    }

    private fun updateTriggerVisibility(trigger: String) {
        binding.layoutRoutineTime.visibility = if (trigger == "daily" || trigger == "weekly") View.VISIBLE else View.GONE
        binding.layoutWeeklyDays.visibility = if (trigger == "weekly") View.VISIBLE else View.GONE
    }

    private fun showTimePicker() {
        val c = Calendar.getInstance()
        TimePickerDialog(requireContext(), { _, h, m ->
            binding.editTextRoutineTime.setText("%02d:%02d".format(h, m))
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
    }

    private fun saveRoutine() {
        val name = binding.editTextRoutineName.text.toString().trim()
        if (name.isEmpty()) { binding.layoutRoutineName.error = "Name required"; return }
        
        val trigger = when (binding.radioGroupTrigger.checkedRadioButtonId) {
            R.id.radioDaily -> "daily"
            R.id.radioWeekly -> "weekly"
            R.id.radioStartup -> "startup"
            else -> "manual"
        }

        val days = if (trigger == "weekly") {
            mutableListOf<String>().apply {
                if (binding.chipMon.isChecked) add("Mon")
                if (binding.chipTue.isChecked) add("Tue")
                if (binding.chipWed.isChecked) add("Wed")
                if (binding.chipThu.isChecked) add("Thu")
                if (binding.chipFri.isChecked) add("Fri")
                if (binding.chipSat.isChecked) add("Sat")
                if (binding.chipSun.isChecked) add("Sun")
            }
        } else emptyList()

        val body = RoutineCreate(
            name = name,
            trigger = trigger,
            time = binding.editTextRoutineTime.text.toString().ifBlank { null },
            days = days,
            prompt = binding.editTextRoutinePrompt.text.toString()
        )

        if (routineToEdit == null) {
            viewModel.createRoutine(body)
        } else {
            viewModel.updateRoutine(routineToEdit!!.id, mapOf(
                "name" to body.name,
                "trigger_type" to body.trigger,
                "schedule_time" to body.time,
                "schedule_days" to body.days,
                "prompt" to body.prompt
            ))
        }
        dismiss()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }

    companion object {
        fun newInstance(routine: Routine? = null): RoutineCreateEditBottomSheet {
            val fragment = RoutineCreateEditBottomSheet()
            if (routine != null) {
                val args = Bundle()
                args.putSerializable("routine", routine)
                fragment.arguments = args
            }
            return fragment
        }
    }
}
