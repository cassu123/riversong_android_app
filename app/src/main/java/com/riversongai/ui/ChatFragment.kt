package com.riversongai.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
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
import java.util.Locale

import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.core.widget.addTextChangedListener
import com.google.android.material.chip.Chip
import com.riversongai.data.model.ChatModel
import com.riversongai.ui.adapter.ChatHistoryAdapter

class ChatFragment : Fragment(), TextToSpeech.OnInitListener {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val chatViewModel: ChatViewModel by viewModel()
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var historyAdapter: ChatHistoryAdapter

    private var tts: TextToSpeech? = null
    private var isTtsEnabled = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tts = TextToSpeech(requireContext(), this)

        setupChatList()
        setupHistoryList()
        setupInput()
        setupListeners()
        observeViewModel()

        arguments?.getString("message")?.let {
            if (it.isNotBlank()) {
                chatViewModel.sendMessage(it)
                arguments?.remove("message") // Only send once
            }
        }
    }

    private fun setupChatList() {
        chatAdapter = ChatAdapter()
        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }
    }

    private fun setupHistoryList() {
        historyAdapter = ChatHistoryAdapter { session ->
            chatViewModel.loadHistoryDetail(session.id)
        }
        binding.recyclerViewHistory.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupInput() {
        binding.editTextMessage.addTextChangedListener {
            binding.buttonSend.isEnabled = !it.isNullOrBlank() && chatViewModel.isConnected.value == true
        }

        binding.editTextMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }

        binding.buttonSend.setOnClickListener { sendMessage() }

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

    private fun setupListeners() {
        binding.buttonClearHistory.setOnClickListener {
            chatViewModel.clearHistory()
        }

        binding.buttonReconnect.setOnClickListener {
            if (chatViewModel.isReplayMode.value == true) {
                chatViewModel.exitReplayMode()
            } else {
                binding.buttonReconnect.isVisible = false
                chatViewModel.reconnect()
            }
        }

        binding.buttonTtsToggle.setOnClickListener {
            isTtsEnabled = !isTtsEnabled
            updateTtsIcon()
        }

        binding.buttonHistory.setOnClickListener {
            toggleHistory(true)
            chatViewModel.loadHistory()
        }

        binding.buttonCloseHistory.setOnClickListener {
            toggleHistory(false)
        }

        binding.containerHistory.setOnClickListener {
            toggleHistory(false)
        }
    }

    private fun toggleHistory(show: Boolean) {
        if (show) {
            binding.containerHistory.isVisible = true
            val animate = TranslateAnimation(300f * resources.displayMetrics.density, 0f, 0f, 0f)
            animate.duration = 300
            binding.containerHistory.getChildAt(0).startAnimation(animate)
        } else {
            val animate = TranslateAnimation(0f, 300f * resources.displayMetrics.density, 0f, 0f)
            animate.duration = 300
            animate.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    binding.containerHistory.isVisible = false
                }
            })
            binding.containerHistory.getChildAt(0).startAnimation(animate)
        }
    }

    private fun observeViewModel() {
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
                    binding.textViewStatus.text = "River Song is thinking…"
                    binding.buttonSend.isEnabled = false
                    binding.buttonMic.isEnabled = false
                }
                "speaking" -> {
                    binding.textViewStatus.isVisible = true
                    binding.textViewStatus.text = "River Song is speaking…"
                    binding.buttonSend.isEnabled = false
                    binding.buttonMic.isEnabled = false
                }
                "listening" -> {
                    binding.textViewStatus.isVisible = true
                    binding.textViewStatus.text = "Listening…"
                    binding.buttonMic.setImageResource(R.drawable.ic_mic)
                    binding.buttonSend.isEnabled = false
                }
                else -> {
                    binding.textViewStatus.isVisible = false
                    binding.buttonSend.isEnabled = binding.editTextMessage.text?.isNotBlank() == true
                    binding.buttonMic.isEnabled = true
                    binding.buttonMic.setImageResource(R.drawable.ic_mic)
                }
            }
        }

        chatViewModel.isConnected.observe(viewLifecycleOwner) { connected ->
            updateStatusChip(connected, chatViewModel.isReplayMode.value ?: false)
        }

        chatViewModel.isReplayMode.observe(viewLifecycleOwner) { isReplay ->
            updateStatusChip(chatViewModel.isConnected.value ?: false, isReplay)
            if (isReplay) {
                binding.buttonReconnect.isVisible = true
                binding.buttonReconnect.setImageResource(R.drawable.ic_close)
                Toast.makeText(context, "Viewing history (Replay Mode)", Toast.LENGTH_SHORT).show()
            } else {
                binding.buttonReconnect.setImageResource(R.drawable.ic_refresh)
            }
        }

        chatViewModel.responseCompleteEvent.observe(viewLifecycleOwner) { text ->
            text?.let {
                if (isTtsEnabled) speak(it)
                chatViewModel.clearResponseCompleteEvent()
            }
        }
    }

    private fun updateStatusChip(connected: Boolean, isReplay: Boolean) {
        binding.chipConnectionStatus.apply {
            if (isReplay) {
                text = "REPLAY"
                setChipBackgroundColorResource(R.color.river_song_secondary_container)
            } else {
                text = if (connected) "Connected" else "Connecting…"
                setChipBackgroundColorResource(
                    if (connected) R.color.river_song_success_container else R.color.river_song_error_container
                )
            }
        }
    }

    private fun populateModelChips(models: List<ChatModel>) {
        binding.chipGroupModels.removeAllViews()
        models.forEach { model ->
            val chip = Chip(requireContext()).apply {
                text = model.name
                isCheckable = true
                isCheckedIconVisible = true
                id = View.generateViewId()
                setOnClickListener {
                    chatViewModel.selectModel(model)
                }
            }
            binding.chipGroupModels.addView(chip)
            if (chatViewModel.selectedModel.value == null && models.indexOf(model) == 0) {
                chip.isChecked = true
                chatViewModel.selectModel(model)
            } else if (chatViewModel.selectedModel.value?.id == model.id) {
                chip.isChecked = true
            }
        }
    }

    private fun sendMessage() {
        val text = binding.editTextMessage.text.toString().trim()
        if (text.isBlank()) return
        binding.editTextMessage.text?.clear()
        chatViewModel.sendMessage(text)
    }

    private fun updateTtsIcon() {
        binding.buttonTtsToggle.setIconResource(
            if (isTtsEnabled) R.drawable.ic_volume_up 
            else R.drawable.ic_volume_off
        )
    }

    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "response_id")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
        }
    }

    private fun hasMicPermission(): Boolean =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    override fun onDestroyView() {
        super.onDestroyView()
        chatViewModel.cancelVoiceIfActive()
        tts?.stop()
        tts?.shutdown()
        _binding = null
    }
}
