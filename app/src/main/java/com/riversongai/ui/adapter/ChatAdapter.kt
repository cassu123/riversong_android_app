package com.riversongai.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.riversongai.data.model.ChatMessage
import com.riversongai.databinding.ItemChatAiBinding
import com.riversongai.databinding.ItemChatUserBinding

private const val TYPE_USER = 0
private const val TYPE_AI = 1

class ChatAdapter : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DiffCallback()) {

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).isUser) TYPE_USER else TYPE_AI

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_USER) {
            UserViewHolder(ItemChatUserBinding.inflate(inflater, parent, false))
        } else {
            AiViewHolder(ItemChatAiBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = getItem(position)
        when (holder) {
            is UserViewHolder -> holder.bind(msg)
            is AiViewHolder -> holder.bind(msg)
        }
    }

    class UserViewHolder(private val binding: ItemChatUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: ChatMessage) {
            binding.textViewMessage.text = msg.content
        }
    }

    class AiViewHolder(private val binding: ItemChatAiBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: ChatMessage) {
            binding.textViewMessage.text = msg.content
            binding.viewCursor.visibility = if (msg.isStreaming)
                android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage) =
            oldItem.timestamp == newItem.timestamp && oldItem.role == newItem.role
        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage) =
            oldItem == newItem
    }
}
