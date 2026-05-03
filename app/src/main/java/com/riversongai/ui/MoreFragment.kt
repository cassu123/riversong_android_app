package com.riversongai.ui

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

    data class MoreItem(
        val title: String,
        val subtitle: String,
        val iconRes: Int,
        val navAction: Int
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val items = listOf(
            MoreItem("Memory", "Facts River Song knows about you", R.drawable.ic_memory, R.id.action_moreFragment_to_memoryFragment),
            MoreItem("Routines", "Scheduled tasks & automations", R.drawable.ic_routines, R.id.action_moreFragment_to_routinesFragment),
            MoreItem("Settings", "AI model & preferences", R.drawable.ic_settings_nav, R.id.action_moreFragment_to_settingsFragment),
            MoreItem("Feed Settings", "Configure news, weather & stocks", R.drawable.ic_feeds, R.id.action_moreFragment_to_feedSettingsFragment)
        )

        binding.recyclerViewMore.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerViewMore.adapter = MoreGridAdapter(items) { item ->
            findNavController().navigate(item.navAction)
        }
    }

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
