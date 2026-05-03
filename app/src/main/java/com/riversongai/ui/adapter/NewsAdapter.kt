package com.riversongai.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.riversongai.R
import com.riversongai.data.model.NewsArticle

class NewsAdapter(
    private val onClick: (NewsArticle) -> Unit
) : ListAdapter<NewsArticle, NewsAdapter.NewsViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    class NewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTitle: TextView = view.findViewById(R.id.textViewNewsTitle)
        private val tvSource: TextView = view.findViewById(R.id.textViewNewsSource)
        private val tvSummary: TextView = view.findViewById(R.id.textViewNewsSummary)

        fun bind(article: NewsArticle, onClick: (NewsArticle) -> Unit) {
            tvTitle.text = article.title
            tvSource.text = article.source.ifBlank { "Unknown source" }
            tvSummary.text = article.summary ?: ""
            tvSummary.visibility = if (article.summary.isNullOrBlank()) View.GONE else View.VISIBLE
            itemView.setOnClickListener { onClick(article) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<NewsArticle>() {
            override fun areItemsTheSame(a: NewsArticle, b: NewsArticle) = a.url == b.url
            override fun areContentsTheSame(a: NewsArticle, b: NewsArticle) = a == b
        }
    }
}
