package com.riversongai.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.riversongai.R
import com.riversongai.databinding.FragmentFeedsStocksBinding
import com.riversongai.ui.adapter.StockAdapter
import com.riversongai.ui.viewmodel.FeedsViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class StocksFragment : Fragment(R.layout.fragment_feeds_stocks) {

    private var _binding: FragmentFeedsStocksBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FeedsViewModel by sharedViewModel()
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
            stockAdapter.submitList(stocks)
            binding.textViewStocksEmpty.visibility = if (stocks.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
