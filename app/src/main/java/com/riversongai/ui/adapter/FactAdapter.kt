package com.riversongai.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.riversongai.R
import com.riversongai.data.model.Fact

class FactAdapter(
    private val onDelete: (Fact) -> Unit
) : ListAdapter<Fact, FactAdapter.FactViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fact, parent, false)
        return FactViewHolder(view)
    }

    override fun onBindViewHolder(holder: FactViewHolder, position: Int) {
        holder.bind(getItem(position), onDelete)
    }

    class FactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvKey: TextView = view.findViewById(R.id.textViewFactKey)
        private val tvValue: TextView = view.findViewById(R.id.textViewFactValue)
        private val btnDelete: ImageButton = view.findViewById(R.id.buttonDeleteFact)

        fun bind(fact: Fact, onDelete: (Fact) -> Unit) {
            tvKey.text = fact.key
            tvValue.text = fact.value
            btnDelete.setOnClickListener { onDelete(fact) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Fact>() {
            override fun areItemsTheSame(a: Fact, b: Fact) = a.id == b.id
            override fun areContentsTheSame(a: Fact, b: Fact) = a == b
        }
    }
}
