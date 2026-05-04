package com.riversongai.ui.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.riversongai.R
import com.riversongai.data.model.InventoryItem

class InventoryItemAdapter(
    private val onDelete: (InventoryItem) -> Unit,
) : ListAdapter<InventoryItem, InventoryItemAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_inventory_item, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName: TextView     = view.findViewById(R.id.tvItemName)
        private val tvCategory: TextView = view.findViewById(R.id.tvItemCategory)
        private val tvLocation: TextView = view.findViewById(R.id.tvItemLocation)
        private val tvCost: TextView     = view.findViewById(R.id.tvItemCost)
        private val tvStatus: TextView   = view.findViewById(R.id.tvItemStatus)
        private val btnDel: ImageView    = view.findViewById(R.id.btnDeleteItem)

        fun bind(item: InventoryItem) {
            tvName.text = item.name
            tvCategory.text = item.category
            tvLocation.text = if (item.location.isNotBlank()) "  ·  ${item.location}" else ""
            tvCost.text = if (item.replacementCost != null && item.replacementCost > 0)
                "Repl. value: $%.2f".format(item.replacementCost) else ""
            tvCost.visibility = if (tvCost.text.isBlank()) View.GONE else View.VISIBLE

            val (label, bg, fg) = when (item.assetStatus) {
                "Serviceable"   -> Triple("SERVICEABLE",   "#1a3a1a", "#3dcc79")
                "Unserviceable" -> Triple("UNSERVICEABLE", "#3a1a1a", "#FFB4AB")
                "Missing"       -> Triple("MISSING",       "#3a2a0a", "#FFB86C")
                "In-Use"        -> Triple("IN USE",        "#1a2a3a", "#96CBFF")
                else            -> Triple(item.assetStatus.uppercase(), "#2a2a2a", "#BFC8CE")
            }
            tvStatus.text = label
            tvStatus.setTextColor(Color.parseColor(fg))
            val badge = GradientDrawable().apply {
                cornerRadius = 4f * itemView.resources.displayMetrics.density
                setColor(Color.parseColor(bg))
            }
            tvStatus.background = badge

            btnDel.setOnClickListener { onDelete(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<InventoryItem>() {
            override fun areItemsTheSame(a: InventoryItem, b: InventoryItem) = a.id == b.id
            override fun areContentsTheSame(a: InventoryItem, b: InventoryItem) = a == b
        }
    }
}
