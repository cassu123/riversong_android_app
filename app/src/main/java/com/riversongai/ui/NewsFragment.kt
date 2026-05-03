package com.riversongai.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.riversongai.R
import com.riversongai.databinding.FragmentFeedsNewsBinding
import com.riversongai.ui.adapter.NewsAdapter
import com.riversongai.ui.viewmodel.FeedsViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class NewsFragment : Fragment(R.layout.fragment_feeds_news) {

    private var _binding: FragmentFeedsNewsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FeedsViewModel by sharedViewModel()
    private lateinit var newsAdapter: NewsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFeedsNewsBinding.bind(view)

        newsAdapter = NewsAdapter(onClick = { article ->
            if (article.url.isNotBlank()) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(article.url)))
            }
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

        viewModel.news.observe(viewLifecycleOwner) { articles ->
            newsAdapter.submitList(articles)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
