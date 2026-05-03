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

class RoutineAdapter(
    private val onToggle: (Routine, Boolean) -> Unit,
    private val onRun: (Routine) -> Unit,
    private val onDelete: (Routine) -> Unit
) : ListAdapter<Routine, RoutineAdapter.RoutineViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_routine, parent, false)
        return RoutineViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        holder.bind(getItem(position), onToggle, onRun, onDelete)
    }

    class RoutineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName: TextView = view.findViewById(R.id.textViewRoutineName)
        private val tvTrigger: TextView = view.findViewById(R.id.textViewRoutineTrigger)
        private val tvLastRun: TextView = view.findViewById(R.id.textViewRoutineLastRun)
        private val switchEnabled: SwitchCompat = view.findViewById(R.id.switchRoutineEnabled)
        private val btnRun: ImageButton = view.findViewById(R.id.buttonRunRoutine)
        private val btnDelete: ImageButton = view.findViewById(R.id.buttonDeleteRoutine)

        fun bind(
            routine: Routine,
            onToggle: (Routine, Boolean) -> Unit,
            onRun: (Routine) -> Unit,
            onDelete: (Routine) -> Unit
        ) {
            tvName.text = routine.name
            tvTrigger.text = when (routine.trigger) {
                "schedule" -> "⏰ ${routine.time ?: ""} ${routine.days.joinToString(", ")}"
                "startup" -> "🚀 On startup"
                else -> "▶ Manual"
            }
            tvLastRun.text = if (routine.lastRun != null) "Last: ${routine.lastRun}" else "Never run"
            switchEnabled.isChecked = routine.enabled
            switchEnabled.setOnCheckedChangeListener { _, checked -> onToggle(routine, checked) }
            btnRun.setOnClickListener { onRun(routine) }
            btnDelete.setOnClickListener { onDelete(routine) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Routine>() {
            override fun areItemsTheSame(a: Routine, b: Routine) = a.id == b.id
            override fun areContentsTheSame(a: Routine, b: Routine) = a == b
        }
    }
}
