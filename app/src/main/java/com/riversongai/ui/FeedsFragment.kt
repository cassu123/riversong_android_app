package com.riversongai.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.riversongai.R
import com.riversongai.databinding.FragmentFeedsBinding
import com.riversongai.ui.adapter.NewsAdapter
import com.riversongai.ui.adapter.StockAdapter
import com.riversongai.ui.viewmodel.FeedsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

import com.google.android.material.tabs.TabLayoutMediator
import com.riversongai.ui.adapter.FeedsPagerAdapter

class FeedsFragment : Fragment() {

    private var _binding: FragmentFeedsBinding? = null
    private val binding get() = _binding!!

    private val feedsViewModel: FeedsViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFeedsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewPagerFeeds.adapter = FeedsPagerAdapter(this)
        binding.viewPagerFeeds.offscreenPageLimit = 3
        
        TabLayoutMediator(binding.tabLayoutFeeds, binding.viewPagerFeeds) { tab, position ->
            tab.text = when (position) {
                0 -> "News"
                1 -> "Weather"
                2 -> "Stocks"
                3 -> "Sports"
                else -> ""
            }
        }.attach()

        binding.swipeRefreshFeeds.setOnRefreshListener {
            feedsViewModel.loadAll()
            // Need to tell fragments to refresh or just rely on SharedViewModel
            binding.swipeRefreshFeeds.isRefreshing = false
        }

        feedsViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                feedsViewModel.clearError()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
