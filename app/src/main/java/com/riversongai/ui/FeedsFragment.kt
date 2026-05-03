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
import com.riversongai.databinding.FragmentFeedsBinding
import com.riversongai.ui.adapter.NewsAdapter
import com.riversongai.ui.adapter.StockAdapter
import com.riversongai.ui.viewmodel.FeedsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class FeedsFragment : Fragment() {

    private var _binding: FragmentFeedsBinding? = null
    private val binding get() = _binding!!

    private val feedsViewModel: FeedsViewModel by viewModel()
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var stockAdapter: StockAdapter

    private var currentTab = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFeedsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newsAdapter = NewsAdapter(onClick = { article ->
            if (article.url.isNotBlank()) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(article.url)))
            }
        })

        stockAdapter = StockAdapter()

        binding.recyclerViewNews.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = newsAdapter
        }

        binding.recyclerViewStocks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stockAdapter
        }

        binding.swipeRefreshFeeds.apply {
            val typedValue = android.util.TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
            setColorSchemeColors(typedValue.data)
            setOnRefreshListener { feedsViewModel.loadAll() }
        }

        setupTabs()
        observeViewModel()
    }

    private fun setupTabs() {
        listOf("News", "Weather", "Stocks").forEach { name ->
            binding.tabLayoutFeeds.addTab(binding.tabLayoutFeeds.newTab().setText(name))
        }

        binding.tabLayoutFeeds.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentTab = tab.position
                updateVisibility()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun updateVisibility() {
        binding.recyclerViewNews.isVisible = currentTab == 0
        binding.scrollViewWeather.isVisible = currentTab == 1
        binding.recyclerViewStocks.isVisible = currentTab == 2

        binding.textViewNewsEmpty.isVisible = false
        binding.textViewStocksEmpty.isVisible = false
    }

    private fun observeViewModel() {
        feedsViewModel.news.observe(viewLifecycleOwner) { articles ->
            newsAdapter.submitList(articles)
            if (currentTab == 0) {
                binding.textViewNewsEmpty.isVisible = articles.isEmpty()
            }
        }

        feedsViewModel.stocks.observe(viewLifecycleOwner) { stocks ->
            stockAdapter.submitList(stocks)
            if (currentTab == 2) {
                binding.textViewStocksEmpty.isVisible = stocks.isEmpty()
            }
        }
    ...
        feedsViewModel.weather.observe(viewLifecycleOwner) { weather ->
            if (weather != null) {
                val c = weather.current
                binding.textViewWeatherTemp.text = "%.0f°C".format(c.tempC)
                binding.textViewWeatherCondition.text = c.conditionText
                binding.textViewWeatherDetails.text =
                    "Feels like %.0f°C  •  Humidity %d%%  •  Wind %.0f km/h".format(
                        c.feelsLikeC, c.humidity, c.windKph
                    )
                binding.textViewWeatherNotConfigured.isVisible = false
            } else {
                binding.textViewWeatherNotConfigured.isVisible = true
            }
        }

        feedsViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                feedsViewModel.clearError()
            }
        }

        val loadingObs = { _: Boolean ->
            val isLoading = feedsViewModel.isLoadingNews.value == true ||
                           feedsViewModel.isLoadingWeather.value == true ||
                           feedsViewModel.isLoadingStocks.value == true
            
            binding.swipeRefreshFeeds.isRefreshing = isLoading
            binding.progressBarFeeds.isVisible = isLoading && !binding.swipeRefreshFeeds.isRefreshing
        }
        feedsViewModel.isLoadingNews.observe(viewLifecycleOwner, loadingObs)
        feedsViewModel.isLoadingWeather.observe(viewLifecycleOwner, loadingObs)
        feedsViewModel.isLoadingStocks.observe(viewLifecycleOwner, loadingObs)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
