package com.riversongai.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
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

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) = holder.bind(getItem(position))

    class StockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvSymbol: TextView = view.findViewById(R.id.textViewStockSymbol)
        private val tvPrice: TextView = view.findViewById(R.id.textViewStockPrice)
        private val tvChange: TextView = view.findViewById(R.id.textViewStockChange)
        
        private val layoutDetails: View = view.findViewById(R.id.layoutStockDetails)
        private val tvOpen: TextView = view.findViewById(R.id.textViewStockOpen)
        private val tvHigh: TextView = view.findViewById(R.id.textViewStockHigh)
        private val tvLow: TextView = view.findViewById(R.id.textViewStockLow)
        private val tvPrevClose: TextView = view.findViewById(R.id.textViewStockPrevClose)

        fun bind(stock: StockQuote) {
            tvSymbol.text = stock.ticker
            tvPrice.text = "$%.2f".format(stock.price)
            
            val sign = if (stock.change >= 0) "+" else ""
            val icon = if (stock.up) "▲" else "▼"
            tvChange.text = "%s %s%.2f (%.2f%%)".format(icon, sign, stock.change, stock.changePct)
            
            val color = if (stock.up) Color.parseColor("#2E7D32") else Color.parseColor("#C62828")
            tvChange.setTextColor(color)

            val detailsExist = stock.open.isNotBlank() || stock.high.isNotBlank() || stock.low.isNotBlank() || stock.prevClose.isNotBlank()
            layoutDetails.isVisible = detailsExist
            if (detailsExist) {
                tvOpen.text = "O: ${stock.open}"
                tvHigh.text = "H: ${stock.high}"
                tvLow.text = "L: ${stock.low}"
                tvPrevClose.text = "P: ${stock.prevClose}"
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<StockQuote>() {
            override fun areItemsTheSame(a: StockQuote, b: StockQuote) = a.ticker == b.ticker
            override fun areContentsTheSame(a: StockQuote, b: StockQuote) = a == b
        }
    }
}
