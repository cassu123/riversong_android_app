package com.riversongai.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.riversongai.R
import com.riversongai.data.model.StockQuote

class StockAdapter : ListAdapter<StockQuote, StockAdapter.StockViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_stock, parent, false)
        return StockViewHolder(view)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class StockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvSymbol: TextView = view.findViewById(R.id.textViewStockSymbol)
        private val tvName: TextView = view.findViewById(R.id.textViewStockName)
        private val tvPrice: TextView = view.findViewById(R.id.textViewStockPrice)
        private val tvChange: TextView = view.findViewById(R.id.textViewStockChange)

        fun bind(stock: StockQuote) {
            tvSymbol.text = stock.symbol
            tvName.text = stock.name ?: ""
            tvPrice.text = "$%.2f".format(stock.price)
            val sign = if (stock.change >= 0) "+" else ""
            tvChange.text = "$sign%.2f (%.2f%%)".format(stock.change, stock.changePercent)
            tvChange.setTextColor(if (stock.change >= 0) Color.parseColor("#2E7D32") else Color.parseColor("#C62828"))
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<StockQuote>() {
            override fun areItemsTheSame(a: StockQuote, b: StockQuote) = a.symbol == b.symbol
            override fun areContentsTheSame(a: StockQuote, b: StockQuote) = a == b
        }
    }
}
