package com.riversongai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.riversongai.R
import com.riversongai.data.model.AnalyticsSnapshot
import com.riversongai.databinding.FragmentAnalyticsBinding
import com.riversongai.databinding.ItemAnalyticsPlatformBinding
import com.riversongai.ui.viewmodel.AnalyticsViewModel
import com.riversongai.utils.UIStyleManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class AnalyticsFragment : Fragment(R.layout.fragment_analytics) {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AnalyticsViewModel by viewModel()
    
    private lateinit var platformAdapter: PlatformAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAnalyticsBinding.bind(view)

        setupUI()
        applyUIStyle()
        observeViewModel()
        
        viewModel.loadData()
    }

    private fun setupUI() {
        platformAdapter = PlatformAdapter(
            onGetInsights = { platform -> viewModel.loadPlatformSummary(platform) },
            getSummary = { platform -> viewModel.platformSummaries.value?.get(platform) }
        )
        binding.recyclerViewPlatforms.apply {
            adapter = platformAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadData()
        }

        binding.chipGroupDateRange.setOnCheckedStateChangeListener { _, checkedIds ->
            val days = when (checkedIds.firstOrNull()) {
                R.id.chip7d -> 7
                R.id.chip90d -> 90
                else -> 30
            }
            viewModel.loadData(days)
        }

        binding.fabAddSnapshot.setOnClickListener {
            showAddSnapshotDialog()
        }
    }

    private fun showAddSnapshotDialog() {
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        val ctx = requireContext()
        val layout = android.widget.LinearLayout(ctx).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val etPlatform = com.google.android.material.textfield.TextInputEditText(ctx).apply { hint = "Platform (e.g. tiktok, shopify)" }
        val etDate = com.google.android.material.textfield.TextInputEditText(ctx).apply { 
            hint = "Date (YYYY-MM-DD)"
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
            setText(today)
        }
        val etMetricName = com.google.android.material.textfield.TextInputEditText(ctx).apply { hint = "Metric Name (e.g. followers)" }
        val etMetricValue = com.google.android.material.textfield.TextInputEditText(ctx).apply { 
            hint = "Value"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        val btnSave = com.google.android.material.button.MaterialButton(ctx).apply {
            text = "Add Snapshot"
            setOnClickListener {
                val platform = etPlatform.text.toString().lowercase()
                val date = etDate.text.toString()
                val mName = etMetricName.text.toString().lowercase()
                val mVal = etMetricValue.text.toString().toDoubleOrNull() ?: 0.0

                if (platform.isBlank() || mName.isBlank()) return@setOnClickListener

                viewModel.addSnapshot(com.riversongai.data.model.SnapshotCreate(
                    platform = platform,
                    date = date,
                    metrics = mapOf(mName to mVal)
                ))
                dialog.dismiss()
            }
        }

        layout.addView(etPlatform); layout.addView(etDate); layout.addView(etMetricName); layout.addView(etMetricValue); layout.addView(btnSave)
        dialog.setContentView(layout)
        dialog.show()
    }

    private fun applyUIStyle() {
        val ctx = requireContext()
        binding.cardRevenue.setCardBackgroundColor(UIStyleManager.resolveCardColor(ctx, 1))
        binding.cardOrders.setCardBackgroundColor(UIStyleManager.resolveCardColor(ctx, 1))
        binding.cardTopPlatform.setCardBackgroundColor(UIStyleManager.resolveCardColor(ctx, 1))
        binding.cardActivePlatforms.setCardBackgroundColor(UIStyleManager.resolveCardColor(ctx, 1))
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = it
            binding.swipeRefresh.isRefreshing = it
        }

        viewModel.snapshots.observe(viewLifecycleOwner) { snapshots ->
            updateSummaryBar(snapshots)
            platformAdapter.submitList(groupSnapshots(snapshots))
            binding.textViewEmpty.isVisible = snapshots.isEmpty()
        }

        viewModel.platformSummaries.observe(viewLifecycleOwner) {
            platformAdapter.notifyDataSetChanged()
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun updateSummaryBar(snapshots: List<AnalyticsSnapshot>) {
        val totalRevenue = snapshots.sumOf { it.metrics.revenue ?: 0.0 }
        val totalOrders = snapshots.sumOf { it.metrics.orders ?: 0 }
        val topPlatform = snapshots.groupBy { it.platform }
            .maxByOrNull { entry -> entry.value.sumOf { it.metrics.followers ?: 0 } }
            ?.key ?: "--"
        val activePlatforms = snapshots.map { it.platform }.distinct().size

        binding.textViewTotalRevenue.text = "$%.2f".format(totalRevenue)
        binding.textViewTotalOrders.text = totalOrders.toString()
        binding.textViewTopPlatform.text = topPlatform.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        binding.textViewActivePlatforms.text = activePlatforms.toString()
    }

    private fun groupSnapshots(snapshots: List<AnalyticsSnapshot>): List<PlatformData> {
        return snapshots.groupBy { it.platform }.map { (platform, list) ->
            PlatformData(platform, list.sortedByDescending { it.date })
        }
    }

    data class PlatformData(val platform: String, val snapshots: List<AnalyticsSnapshot>)

    private inner class PlatformAdapter(
        private val onGetInsights: (String) -> Unit,
        private val getSummary: (String) -> String?
    ) : ListAdapter<PlatformData, PlatformAdapter.VH>(DiffCallback) {

        private var expandedPlatform: String? = null

        inner class VH(val binding: ItemAnalyticsPlatformBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(ItemAnalyticsPlatformBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val data = getItem(position)
            val latest = data.snapshots.firstOrNull() ?: return
            
            holder.binding.textViewPlatformName.text = data.platform.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            holder.binding.imageViewPlatformIcon.setImageResource(getPlatformIcon(data.platform))
            
            val primaryMetricValue = latest.metrics.followers ?: latest.metrics.views ?: 0
            holder.binding.textViewPrimaryMetricValue.text = primaryMetricValue.toString()
            holder.binding.textViewPrimaryMetricLabel.text = if (latest.metrics.followers != null) "Followers" else "Views"
            
            if (latest.metrics.revenue != null) {
                holder.binding.layoutRevenue.isVisible = true
                holder.binding.textViewRevenueValue.text = "$%.2f".format(latest.metrics.revenue)
            } else {
                holder.binding.layoutRevenue.isVisible = false
            }

            // Expanded state
            val isExpanded = expandedPlatform == data.platform
            holder.binding.layoutExpandedMetrics.isVisible = isExpanded
            
            if (isExpanded) {
                val metricsText = StringBuilder()
                latest.metrics.followers?.let { metricsText.append("Followers: $it\n") }
                latest.metrics.views?.let { metricsText.append("Views: $it\n") }
                latest.metrics.revenue?.let { metricsText.append("Revenue: $%.2f\n".format(it)) }
                latest.metrics.orders?.let { metricsText.append("Orders: $it") }
                holder.binding.textViewAllMetrics.text = metricsText.toString()
                
                val summary = getSummary(data.platform)
                if (summary != null) {
                    holder.binding.textViewAiInsights.text = summary
                    holder.binding.textViewAiInsights.isVisible = true
                    holder.binding.progressBarInsights.isVisible = false
                    holder.binding.buttonGetInsights.isVisible = false
                } else {
                    holder.binding.textViewAiInsights.isVisible = false
                    holder.binding.buttonGetInsights.isVisible = true
                    holder.binding.progressBarInsights.isVisible = false
                }
            }

            holder.binding.cardPlatform.setOnClickListener {
                expandedPlatform = if (isExpanded) null else data.platform
                notifyItemChanged(position)
            }

            holder.binding.buttonGetInsights.setOnClickListener {
                holder.binding.progressBarInsights.isVisible = true
                holder.binding.buttonGetInsights.isVisible = false
                onGetInsights(data.platform)
            }
        }

        private fun getPlatformIcon(platform: String): Int {
            return when (platform.lowercase()) {
                "tiktok", "instagram" -> R.drawable.ic_feeds
                "youtube" -> R.drawable.ic_history
                "facebook" -> R.drawable.ic_profile
                "twitter", "x" -> R.drawable.ic_chat
                "amazon", "etsy", "ebay", "shopify" -> R.drawable.ic_store
                "pinterest" -> R.drawable.ic_links
                else -> R.drawable.ic_analytics
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<PlatformData>() {
        override fun areItemsTheSame(oldItem: PlatformData, newItem: PlatformData) = oldItem.platform == newItem.platform
        override fun areContentsTheSame(oldItem: PlatformData, newItem: PlatformData) = oldItem == newItem
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
