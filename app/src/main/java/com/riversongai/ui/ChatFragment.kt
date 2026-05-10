package com.riversongai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.riversongai.R
import com.riversongai.data.model.ChatMessage
import com.riversongai.data.model.ModelEntry
import com.riversongai.databinding.FragmentChatBinding
import com.riversongai.databinding.ItemChatAiBinding
import com.riversongai.databinding.ItemChatUserBinding
import com.riversongai.ui.viewmodel.ChatViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by viewModel()
    private lateinit var adapter: ChatAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChatBinding.bind(view)

        adapter = ChatAdapter()
        binding.recyclerViewChat.apply {
            adapter = this@ChatFragment.adapter
            layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
        }

        binding.buttonSend.setOnClickListener {
            val text = binding.editTextMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.sendMessage(text)
                binding.editTextMessage.text?.clear()
            }
        }

        binding.buttonReset.setOnClickListener {
            viewModel.resetChat()
        }

        setupModelSelector()
        observeViewModel()
    }

    private fun setupModelSelector() {
        viewModel.models.observe(viewLifecycleOwner) { models ->
            val names = models.map { it.displayName }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, names)
            binding.spinnerModel.adapter = adapter
            
            viewModel.selectedModel.value?.let { selected ->
                val idx = models.indexOfFirst { it.modelId == selected.modelId }
                if (idx != -1) binding.spinnerModel.setSelection(idx)
            }
        }

        binding.spinnerModel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.models.value?.get(position)?.let { viewModel.selectModel(it) }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun observeViewModel() {
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.submitList(messages)
            binding.recyclerViewChat.scrollToPosition(messages.size - 1)
        }

        viewModel.streamingResponse.observe(viewLifecycleOwner) { chunk ->
            binding.textViewStreaming.isVisible = chunk.isNotEmpty()
            binding.textViewStreaming.text = chunk
            if (chunk.isNotEmpty()) binding.recyclerViewChat.scrollToPosition(adapter.itemCount)
        }

        viewModel.isThinking.observe(viewLifecycleOwner) { thinking ->
            binding.progressBarThinking.isVisible = thinking
            binding.buttonSend.isEnabled = !thinking
        }

        viewModel.error.observe(viewLifecycleOwner) { err ->
            err?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show(); viewModel.clearError() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class ChatAdapter : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DIFF) {
        override fun getItemViewType(position: Int): Int = if (getItem(position).role == "user") 0 else 1
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return if (viewType == 0) UserVH(ItemChatUserBinding.inflate(inflater, parent, false))
            else AiVH(ItemChatAiBinding.inflate(inflater, parent, false))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val msg = getItem(position)
            if (holder is UserVH) holder.b.textViewMessage.text = msg.content
            else if (holder is AiVH) holder.b.textViewMessage.text = msg.content
        }

        inner class UserVH(val b: ItemChatUserBinding) : RecyclerView.ViewHolder(b.root)
        inner class AiVH(val b: ItemChatAiBinding) : RecyclerView.ViewHolder(b.root)
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(a: ChatMessage, b: ChatMessage) = a == b
            override fun areContentsTheSame(a: ChatMessage, b: ChatMessage) = a == b
        }
    }
}
