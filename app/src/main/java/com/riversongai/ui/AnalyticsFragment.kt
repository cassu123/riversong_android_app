package com.riversongai.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.riversongai.R
import com.riversongai.data.model.AnalyticsSnapshot
import com.riversongai.databinding.FragmentAnalyticsBinding
import com.riversongai.databinding.ItemAnalyticsPlatformBinding
import com.riversongai.ui.viewmodel.AnalyticsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.sin

class AnalyticsFragment : Fragment(R.layout.fragment_analytics) {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AnalyticsViewModel by viewModel()
    
    private lateinit var adapter: PlatformAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAnalyticsBinding.bind(view)

        adapter = PlatformAdapter()
        binding.recyclerViewPlatforms.apply {
            adapter = this@AnalyticsFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.swipeRefresh.setOnRefreshListener { viewModel.loadData() }

        binding.chipGroupRange.setOnCheckedStateChangeListener { _, checkedIds ->
            val days = when (checkedIds.firstOrNull()) {
                R.id.chip7d -> 7
                R.id.chip90d -> 90
                else -> 30
            }
            viewModel.loadData(days)
        }

        binding.btnGenerateReport.setOnClickListener {
            viewModel.generateBusinessReport()
        }

        observeViewModel()
        viewModel.loadData()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { binding.swipeRefresh.isRefreshing = it }
        
        viewModel.isGeneratingReport.observe(viewLifecycleOwner) { generating ->
            binding.btnGenerateReport.isEnabled = !generating
            binding.btnGenerateReport.text = if (generating) "ANALYZING..." else "GENERATE"
        }

        viewModel.businessReport.observe(viewLifecycleOwner) { report ->
            binding.textViewReport.text = report ?: "Click generate for a natural-language performance summary."
        }

        viewModel.platforms.observe(viewLifecycleOwner) { plats ->
            // Map snapshots to adapter
            viewModel.snapshots.observe(viewLifecycleOwner) { snaps ->
                adapter.submitList(plats.map { p ->
                    val key = p["key"] as? String ?: ""
                    PlatformUIModel(
                        key = key,
                        label = p["label"] as? String ?: key,
                        color = p["color"] as? String ?: "#888888",
                        snapshots = snaps.filter { it.platform == key }
                    )
                })
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { msg ->
            msg?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show(); viewModel.clearError() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class PlatformUIModel(
        val key: String,
        val label: String,
        val color: String,
        val snapshots: List<AnalyticsSnapshot>
    )

    private inner class PlatformAdapter : ListAdapter<PlatformUIModel, PlatformAdapter.VH>(DIFF) {
        inner class VH(val b: ItemAnalyticsPlatformBinding) : RecyclerView.ViewHolder(b.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
            VH(ItemAnalyticsPlatformBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = getItem(position)
            holder.b.textViewPlatformName.text = item.label
            
            val color = try { Color.parseColor(item.color) } catch (e: Exception) { Color.GRAY }
            holder.b.viewConnectionDot.backgroundTintList = ColorStateList.valueOf(color)

            if (item.snapshots.isNotEmpty()) {
                val latest = item.snapshots.last()
                val primaryMetric = latest.metrics.keys.firstOrNull() ?: ""
                val primaryVal = latest.metrics[primaryMetric] ?: 0f
                
                holder.b.textViewPrimaryVal.text = if (primaryMetric == "revenue") "$%.2f".format(primaryVal) else primaryVal.toInt().toString()
                holder.b.textViewMetricLabel.text = primaryMetric.replace("_", " ").uppercase()

                // Calculate delta if possible
                if (item.snapshots.size > 1) {
                    val prev = item.snapshots[item.snapshots.size - 2]
                    val prevVal = prev.metrics[primaryMetric] ?: 0f
                    if (prevVal != 0f) {
                        val delta = ((primaryVal - prevVal) / prevVal) * 100
                        holder.b.textViewDelta.isVisible = true
                        holder.b.textViewDelta.text = "%s%.1f%%".format(if (delta >= 0) "▲ " else "▼ ", kotlin.math.abs(delta))
                        holder.b.textViewDelta.setTextColor(if (delta >= 0) Color.parseColor("#2E7D32") else Color.parseColor("#C62828"))
                    }
                } else {
                    holder.b.textViewDelta.isVisible = false
                }

                drawSparkline(holder.b.layoutSparkline, item.snapshots.map { it.metrics[primaryMetric] ?: 0f }, color)
            } else {
                holder.b.textViewPrimaryVal.text = "--"
                holder.b.textViewDelta.isVisible = false
                holder.b.layoutSparkline.removeAllViews()
            }
        }

        private fun drawSparkline(container: LinearLayout, values: List<Float>, color: Int) {
            container.removeAllViews()
            if (values.size < 2) return
            val max = values.maxOrNull() ?: 1f
            val min = values.minOrNull() ?: 0f
            val range = (max - min).coerceAtLeast(1f)
            
            val bars = values.takeLast(20) // Show last 20 points
            bars.forEach { v ->
                val height = (4 + ((v - min) / range) * 36).toInt()
                val bar = View(container.context).apply {
                    layoutParams = LinearLayout.LayoutParams(0, (height * resources.displayMetrics.density).toInt(), 1f).apply {
                        marginEnd = (1 * resources.displayMetrics.density).toInt()
                    }
                    setBackgroundColor(color)
                    alpha = 0.6f
                }
                container.addView(bar)
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<PlatformUIModel>() {
            override fun areItemsTheSame(a: PlatformUIModel, b: PlatformUIModel) = a.key == b.key
            override fun areContentsTheSame(a: PlatformUIModel, b: PlatformUIModel) = a == b
        }
    }
}
