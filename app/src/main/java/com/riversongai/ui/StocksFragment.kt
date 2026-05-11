package com.riversongai.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.riversongai.R
import com.riversongai.databinding.FragmentFeedsStocksBinding
import com.riversongai.ui.adapter.StockAdapter
import com.riversongai.ui.viewmodel.FeedsViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class StocksFragment : Fragment(R.layout.fragment_feeds_stocks) {

    private var _binding: FragmentFeedsStocksBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FeedsViewModel by activityViewModel()
    private lateinit var stockAdapter: StockAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFeedsStocksBinding.bind(view)

        stockAdapter = StockAdapter()
        binding.recyclerViewStocks.apply {
            adapter = stockAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        viewModel.stocks.observe(viewLifecycleOwner) { stocks ->
            updateUI(stocks)
        }

        viewModel.marketOpen.observe(viewLifecycleOwner) { isOpen ->
            binding.textViewMarketStatus.text = if (isOpen) "US Markets Open" else "US Markets Closed"
            binding.viewMarketStatusDot.backgroundTintList = ColorStateList.valueOf(
                if (isOpen) Color.parseColor("#2E7D32") else Color.parseColor("#C62828")
            )
        }

        setupSortButtons()

        binding.btnGoToStockSettings.setOnClickListener {
            findNavController().navigate(R.id.action_moreFragment_to_feedSettingsFragment)
        }
    }

    private fun updateUI(stocks: List<com.riversongai.data.model.StockQuote>) {
        stockAdapter.submitList(stocks)
        binding.layoutStocksEmpty.isVisible = stocks.isEmpty()

        // Find top gainer
        val topGainer = stocks.filter { it.up }.maxByOrNull { it.changePct }
        if (topGainer != null) {
            binding.cardTopGainer.isVisible = true
            binding.textViewTopGainerSymbol.text = topGainer.ticker
            binding.textViewTopGainerChange.text = "+%.2f%%".format(topGainer.changePct)
        } else {
            binding.cardTopGainer.isVisible = false
        }
    }

    private fun setupSortButtons() {
        binding.btnSortByMove.setOnClickListener {
            val currentList = stockAdapter.currentList.toMutableList()
            currentList.sortByDescending { it.changePct }
            stockAdapter.submitList(currentList)
        }

        binding.btnSortAZ.setOnClickListener {
            val currentList = stockAdapter.currentList.toMutableList()
            currentList.sortBy { it.ticker }
            stockAdapter.submitList(currentList)
        }

        binding.btnSortByPrice.setOnClickListener {
            val currentList = stockAdapter.currentList.toMutableList()
            currentList.sortByDescending { it.price }
            stockAdapter.submitList(currentList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
