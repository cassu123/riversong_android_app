package com.riversongai.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.riversongai.R
import com.riversongai.data.model.Product

class ProductAdapter(
    private val onDelete: (Product) -> Unit,
) : ListAdapter<Product, ProductAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName: TextView   = view.findViewById(R.id.tvProductName)
        private val tvSku: TextView    = view.findViewById(R.id.tvProductSku)
        private val tvCat: TextView    = view.findViewById(R.id.tvProductCategory)
        private val tvPrice: TextView  = view.findViewById(R.id.tvProductPrice)
        private val tvStock: TextView  = view.findViewById(R.id.tvProductStock)
        private val dot: View          = view.findViewById(R.id.viewStockDot)
        private val btnDel: ImageView  = view.findViewById(R.id.btnDeleteProduct)

        fun bind(p: Product) {
            tvName.text  = p.name
            tvSku.text   = p.sku
            tvCat.text   = p.category
            tvPrice.text = if (p.unitPrice > 0) "$%.2f".format(p.unitPrice) else "–"
            val (stockLabel, dotColor) = when {
                p.stockQty == 0          -> "OUT OF STOCK" to Color.parseColor("#FFB4AB")
                p.stockQty <= p.lowStock -> "LOW – ${p.stockQty}" to Color.parseColor("#FFB86C")
                else                     -> "${p.stockQty} in stock" to Color.parseColor("#3dcc79")
            }
            tvStock.text = stockLabel
            dot.background.setTint(dotColor)
            btnDel.setOnClickListener { onDelete(p) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(a: Product, b: Product) = a.id == b.id
            override fun areContentsTheSame(a: Product, b: Product) = a == b
        }
    }
}
