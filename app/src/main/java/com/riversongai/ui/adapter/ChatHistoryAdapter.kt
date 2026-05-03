package com.riversongai.ui.adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.riversongai.data.model.ChatSession
import com.riversongai.databinding.ItemChatHistoryBinding

class ChatHistoryAdapter(
    private val onSessionClick: (ChatSession) -> Unit
) : ListAdapter<ChatSession, ChatHistoryAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = getItem(position)
        holder.bind(session)
    }

    inner class ViewHolder(private val binding: ItemChatHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(session: ChatSession) {
            binding.textViewSessionTitle.text = session.title
            binding.textViewSessionModel.text = session.model
            binding.textViewMessageCount.text = "${session.messageCount} msgs"
            
            binding.textViewSessionDate.text = DateUtils.getRelativeTimeSpanString(
                session.timestamp,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )

            binding.root.setOnClickListener { onSessionClick(session) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ChatSession>() {
        override fun areItemsTheSame(oldItem: ChatSession, newItem: ChatSession): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatSession, newItem: ChatSession): Boolean {
            return oldItem == newItem
        }
    }
}
