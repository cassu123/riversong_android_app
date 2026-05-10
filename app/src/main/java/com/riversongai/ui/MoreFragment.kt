package com.riversongai.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import android.widget.TextView
import com.riversongai.R
import com.riversongai.databinding.FragmentMoreBinding

class MoreFragment : Fragment() {

    private var _binding: FragmentMoreBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isAdmin = requireContext()
            .getSharedPreferences("rs_prefs", Context.MODE_PRIVATE)
            .getBoolean("is_admin", false)

        val items = mutableListOf(
            MoreItem("Memory", "Facts River Song knows about you", R.drawable.ic_memory, R.id.memoryFragment),
            MoreItem("Analytics", "Platform growth & metrics", R.drawable.ic_analytics, R.id.analyticsFragment),
            MoreItem("Culinary", "Recipes & meal planning", R.drawable.ic_memory, R.id.culinaryFragment),
            MoreItem("Reading", "Your digital bookshelf", R.drawable.ic_reading, R.id.readingFragment),
            MoreItem("Google", "Calendar & Gmail integration", R.drawable.ic_feeds, R.id.googleFragment),
            MoreItem("Smart Home", "Control devices & automations", R.drawable.ic_home, R.id.smartHomeControlScreen),
            MoreItem("Settings", "AI model & preferences", R.drawable.ic_settings_nav, R.id.settingsFragment)
        )

        if (isAdmin) {
            items.add(MoreItem("Routines", "Scheduled tasks & automations", R.drawable.ic_routines, R.id.routinesFragment))
            items.add(MoreItem("Home Node", "Advanced HA control", R.drawable.ic_devices, R.id.homeNodeFragment))
            items.add(MoreItem("Users", "Manage team access", R.drawable.ic_profile, R.id.usersFragment))
        }

        binding.recyclerViewMore.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerViewMore.adapter = MoreGridAdapter(items) { item ->
            findNavController().navigate(item.actionId)
        }
    }

    private data class MoreItem(
        val title: String,
        val subtitle: String,
        val iconRes: Int,
        val actionId: Int
    )


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class MoreGridAdapter(
        private val items: List<MoreItem>,
        private val onClick: (MoreItem) -> Unit
    ) : RecyclerView.Adapter<MoreGridAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val icon: ImageView = view.findViewById(R.id.imageViewMoreIcon)
            val title: TextView = view.findViewById(R.id.textViewMoreTitle)
            val subtitle: TextView = view.findViewById(R.id.textViewMoreSubtitle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_more_card, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.icon.setImageResource(item.iconRes)
            holder.title.text = item.title
            holder.subtitle.text = item.subtitle
            holder.itemView.setOnClickListener { onClick(item) }
        }

        override fun getItemCount() = items.size
    }
}
