package com.riversongai.ui.adapter

import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.riversongai.R
import com.riversongai.data.model.Fact
import java.text.SimpleDateFormat
import java.util.Locale

class FactAdapter(
    private val onDelete: (Fact) -> Unit,
    private val onSelect: (String, Boolean) -> Unit
) : ListAdapter<Fact, FactAdapter.FactViewHolder>(DIFF) {

    private val selectedIds = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fact, parent, false)
        return FactViewHolder(view)
    }

    override fun onBindViewHolder(holder: FactViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, selectedIds.contains(item.id), onDelete, onSelect)
    }

    inner class FactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val checkBox: CheckBox = view.findViewById(R.id.checkBoxFact)
        private val tvKey: TextView = view.findViewById(R.id.textViewFactKey)
        private val tvValue: TextView = view.findViewById(R.id.textViewFactValue)
        private val chipSource: Chip = view.findViewById(R.id.chipFactSource)
        private val tvDate: TextView = view.findViewById(R.id.textViewFactDate)
        private val btnDelete: ImageButton = view.findViewById(R.id.buttonDeleteFact)

        private val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        private val outputFormat = SimpleDateFormat("MMM d", Locale.US)

        fun bind(fact: Fact, isSelected: Boolean, onDelete: (Fact) -> Unit, onSelect: (String, Boolean) -> Unit) {
            checkBox.isChecked = isSelected
            tvKey.text = fact.key
            tvValue.text = fact.value

            chipSource.text = fact.source
            if (fact.source.equals("inferred", ignoreCase = true)) {
                setChipColors(chipSource, 
                    com.google.android.material.R.attr.colorSecondaryContainer,
                    com.google.android.material.R.attr.colorOnSecondaryContainer)
            } else {
                setChipColors(chipSource, 
                    com.google.android.material.R.attr.colorSurfaceVariant,
                    com.google.android.material.R.attr.colorOnSurfaceVariant)
            }

            val dateStr = fact.updatedAt ?: fact.createdAt
            tvDate.text = try {
                if (dateStr != null) {
                    val date = inputFormat.parse(dateStr)
                    if (date != null) outputFormat.format(date) else ""
                } else ""
            } catch (e: Exception) { "" }

            btnDelete.setOnClickListener { onDelete(fact) }
            
            checkBox.setOnCheckedChangeListener(null)
            checkBox.setOnCheckedChangeListener { _, checked ->
                if (checked) selectedIds.add(fact.id) else selectedIds.remove(fact.id)
                onSelect(fact.id, checked)
            }
            
            itemView.setOnClickListener { checkBox.toggle() }
        }

        private fun setChipColors(chip: Chip, backgroundAttr: Int, textAttr: Int) {
            val bgColor = resolveColor(backgroundAttr)
            val textColor = resolveColor(textAttr)
            chip.chipBackgroundColor = ColorStateList.valueOf(bgColor)
            chip.setTextColor(textColor)
        }

        private fun resolveColor(@AttrRes attr: Int): Int {
            val typedValue = TypedValue()
            itemView.context.theme.resolveAttribute(attr, typedValue, true)
            return typedValue.data
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Fact>() {
            override fun areItemsTheSame(a: Fact, b: Fact) = a.id == b.id
            override fun areContentsTheSame(a: Fact, b: Fact) = a == b
        }
    }
}
