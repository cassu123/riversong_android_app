package com.riversongai.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.riversongai.R
import com.riversongai.databinding.FragmentChatBinding
import com.riversongai.ui.adapter.ChatAdapter
import com.riversongai.ui.viewmodel.ChatViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val chatViewModel: ChatViewModel by viewModel()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatAdapter = ChatAdapter()
        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }

        chatViewModel.messages.observe(viewLifecycleOwner) { messages ->
            chatAdapter.submitList(messages) {
                if (messages.isNotEmpty()) {
                    binding.recyclerViewChat.smoothScrollToPosition(messages.size - 1)
                }
            }
            binding.textViewEmpty.isVisible = messages.isEmpty()
        }

        chatViewModel.status.observe(viewLifecycleOwner) { status ->
            when (status) {
                "thinking", "transcribing" -> {
                    binding.textViewStatus.isVisible = true
                    binding.textViewStatus.text = getString(R.string.chat_status_thinking)
                    binding.buttonSend.isEnabled = false
                    binding.buttonMic.isEnabled = false
                }
                "speaking" -> {
                    binding.textViewStatus.isVisible = true
                    binding.textViewStatus.text = getString(R.string.chat_status_speaking)
                    binding.buttonSend.isEnabled = false
                    binding.buttonMic.isEnabled = false
                }
                "listening" -> {
                    binding.textViewStatus.isVisible = true
                    binding.textViewStatus.text = getString(R.string.chat_listening)
                    binding.buttonMic.setImageResource(R.drawable.ic_mic)
                    binding.buttonSend.isEnabled = false
                }
                else -> {
                    binding.textViewStatus.isVisible = false
                    binding.buttonSend.isEnabled = true
                    binding.buttonMic.isEnabled = true
                    binding.buttonMic.setImageResource(R.drawable.ic_mic)
                }
            }
        }

        chatViewModel.isConnected.observe(viewLifecycleOwner) { connected ->
            binding.chipConnectionStatus.apply {
                text = if (connected) "Connected" else "Connecting…"
                setChipBackgroundColorResource(
                    if (connected) R.color.status_connected else R.color.status_disconnected
                )
            }
            binding.buttonSend.isEnabled = connected
        }

        chatViewModel.connectionError.observe(viewLifecycleOwner) { error ->
            error?.let {
                binding.chipConnectionStatus.text = "Offline"
                binding.buttonReconnect.isVisible = true
            }
        }

        binding.buttonSend.setOnClickListener { sendMessage() }

        binding.editTextMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }

        binding.buttonReconnect.setOnClickListener {
            binding.buttonReconnect.isVisible = false
            chatViewModel.reconnect()
        }

        binding.buttonClearHistory.setOnClickListener {
            chatViewModel.clearHistory()
        }

        binding.buttonMic.setOnClickListener {
            if (chatViewModel.isRecording) {
                chatViewModel.stopVoiceInput()
            } else {
                if (hasMicPermission()) {
                    chatViewModel.startVoiceInput()
                } else {
                    Toast.makeText(context, "Microphone permission required", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sendMessage() {
        val text = binding.editTextMessage.text.toString().trim()
        if (text.isBlank()) return
        binding.editTextMessage.text?.clear()
        chatViewModel.sendMessage(text)
    }

    private fun hasMicPermission(): Boolean =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    override fun onDestroyView() {
        super.onDestroyView()
        chatViewModel.cancelVoiceIfActive()
        _binding = null
    }
}
