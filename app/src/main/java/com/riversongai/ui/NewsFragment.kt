package com.riversongai.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.riversongai.R
import com.riversongai.databinding.FragmentFeedsNewsBinding
import com.riversongai.ui.adapter.NewsAdapter
import com.riversongai.ui.viewmodel.FeedsViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.util.Locale

class NewsFragment : Fragment(R.layout.fragment_feeds_news) {

    private var _binding: FragmentFeedsNewsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FeedsViewModel by activityViewModel()
    private lateinit var newsAdapter: NewsAdapter
    
    private var refreshTimer: CountDownTimer? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFeedsNewsBinding.bind(view)

        newsAdapter = NewsAdapter(onClick = { article ->
            if (article.url.isNotBlank())
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(article.url)))
        })

        binding.recyclerViewNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.chipGroupNewsCategory.setOnCheckedStateChangeListener { group, checkedIds ->
            val chipId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val chip = group.findViewById<Chip>(chipId)
            viewModel.setNewsCategory(chip.text.toString())
        }

        binding.btnRefreshNow.setOnClickListener {
            viewModel.loadNews()
            startRefreshTimer()
        }

        viewModel.news.observe(viewLifecycleOwner) { articles ->
            newsAdapter.submitList(articles)
            binding.textViewArticleCount.text = "${articles.size} ARTICLES"
        }

        viewModel.preferences.observe(viewLifecycleOwner) {
            startRefreshTimer()
        }

        viewModel.loadPreferences()
    }

    private fun startRefreshTimer() {
        refreshTimer?.cancel()
        val mins = viewModel.preferences.value?.refreshNewsMins ?: 360
        val totalMs = mins.toLong() * 60 * 1000

        refreshTimer = object : CountDownTimer(totalMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val totalSeconds = millisUntilFinished / 1000
                val mm = totalSeconds / 60
                val ss = totalSeconds % 60
                binding.textViewRefreshTimer.text = "Refresh in %02d:%02d".format(mm, ss)
            }

            override fun onFinish() {
                viewModel.loadNews()
                startRefreshTimer()
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        refreshTimer?.cancel()
        refreshTimer = null
        _binding = null
    }
}
