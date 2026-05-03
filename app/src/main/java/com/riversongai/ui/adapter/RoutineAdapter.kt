package com.riversongai.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.riversongai.R
import com.riversongai.data.model.Routine

import android.text.format.DateUtils
import com.riversongai.databinding.ItemRoutineBinding

class RoutineAdapter(
    private val onToggle: (Routine, Boolean) -> Unit,
    private val onRun: (Routine) -> Unit,
    private val onEdit: (Routine) -> Unit,
    private val onDelete: (Routine) -> Unit
) : ListAdapter<Routine, RoutineAdapter.RoutineViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val binding = ItemRoutineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoutineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        holder.bind(getItem(position), onToggle, onRun, onEdit, onDelete)
    }

    class RoutineViewHolder(private val binding: ItemRoutineBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            routine: Routine,
            onToggle: (Routine, Boolean) -> Unit,
            onRun: (Routine) -> Unit,
            onEdit: (Routine) -> Unit,
            onDelete: (Routine) -> Unit
        ) {
            binding.textViewRoutineName.text = routine.name
            
            binding.chipRoutineSchedule.text = when (routine.triggerType) {
                "daily" -> "Daily ${routine.scheduleTime ?: ""}"
                "weekly" -> "Weekly ${routine.scheduleDays?.joinToString(",") ?: ""} ${routine.scheduleTime ?: ""}"
                "startup" -> "On Startup"
                else -> "Manual"
            }

            binding.textViewPromptPreview.text = if (routine.prompt.length > 80) {
                routine.prompt.take(80) + "…"
            } else {
                routine.prompt
            }

            binding.textViewLastRun.text = if (routine.lastRunAt != null) {
                "Last run: ${DateUtils.getRelativeTimeSpanString(routine.lastRunAt, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)}"
            } else {
                "Never run"
            }

            binding.switchRoutineEnabled.isChecked = routine.isEnabled
            binding.switchRoutineEnabled.setOnCheckedChangeListener { _, checked -> onToggle(routine, checked) }
            
            binding.buttonRunRoutine.setOnClickListener { onRun(routine) }
            binding.buttonEditRoutine.setOnClickListener { onEdit(routine) }
            binding.buttonDeleteRoutine.setOnClickListener { onDelete(routine) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Routine>() {
            override fun areItemsTheSame(a: Routine, b: Routine) = a.id == b.id
            override fun areContentsTheSame(a: Routine, b: Routine) = a == b
        }
    }
}
