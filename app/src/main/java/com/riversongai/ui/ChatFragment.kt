package com.riversongai.ui

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.TypedValue
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

import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.content.Context
import android.os.Build

class ChatFragment : Fragment(), TextToSpeech.OnInitListener {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val chatViewModel: ChatViewModel by viewModel()
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var historyAdapter: ChatHistoryAdapter

    private var tts: TextToSpeech? = null
    private var isTtsEnabled = true

    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        tts = TextToSpeech(requireContext(), this)

        setupChatList()
        setupHistoryList()
        setupInput()
        setupListeners()
        observeViewModel()

        arguments?.getString("message")?.let {
            if (it.isNotBlank()) {
                chatViewModel.sendMessage(it)
                arguments?.remove("message")
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
            toggleHistory(false)
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
                    requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 100)
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
        
        binding.cardModelSelector.setOnClickListener {
            binding.scrollViewModels.isVisible = !binding.scrollViewModels.isVisible
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
                    binding.recyclerViewChat.scrollToPosition(messages.size - 1)
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
                    binding.buttonMic.setIconResource(R.drawable.ic_close)
                    binding.buttonSend.isEnabled = false
                }
                else -> {
                    binding.textViewStatus.isVisible = false
                    binding.buttonSend.isEnabled = binding.editTextMessage.text?.isNotBlank() == true
                    binding.buttonMic.isEnabled = true
                    binding.buttonMic.setIconResource(R.drawable.ic_mic)
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
                binding.buttonReconnect.setIconResource(R.drawable.ic_close)
            } else {
                binding.buttonReconnect.setIconResource(R.drawable.ic_refresh)
            }
        }

        chatViewModel.responseCompleteEvent.observe(viewLifecycleOwner) { text ->
            text?.let {
                if (isTtsEnabled) speak(it)
                chatViewModel.clearResponseCompleteEvent()
            }
        }

        chatViewModel.availableModels.observe(viewLifecycleOwner) { models ->
            populateModelChips(models)
        }

        chatViewModel.selectedModel.observe(viewLifecycleOwner) { model ->
            model?.let {
                binding.textViewModelName.text = it.name
                binding.textViewModelSub.text = "${if (it.isLocal) "Local" else "Cloud"} · ${it.provider}"
            }
        }
    }

    private fun updateStatusChip(connected: Boolean, isReplay: Boolean) {
        binding.chipConnectionStatus.apply {
            if (isReplay) {
                text = "REPLAY"
                setChipBackgroundColorResource(android.R.color.darker_gray)
            } else {
                text = if (connected) "Connected" else "Connecting…"
                val color = if (connected) android.R.color.holo_green_dark else android.R.color.holo_red_dark
                chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color))
            }
        }
    }

    private fun populateModelChips(models: List<ChatModel>) {
        binding.chipGroupModels.removeAllViews()
        val primaryContainer = getThemeColor(com.google.android.material.R.attr.colorPrimaryContainer)
        val surfaceVariant = getThemeColor(com.google.android.material.R.attr.colorSurfaceVariant)
        val onPrimaryContainer = getThemeColor(com.google.android.material.R.attr.colorOnPrimaryContainer)
        val onSurfaceVariant = getThemeColor(com.google.android.material.R.attr.colorOnSurfaceVariant)

        models.forEach { model ->
            val chip = Chip(requireContext()).apply {
                text = model.name
                isCheckable = true
                isCheckedIconVisible = false
                
                val isSelected = chatViewModel.selectedModel.value?.id == model.id
                isChecked = isSelected
                
                chipBackgroundColor = ColorStateList.valueOf(if (isSelected) primaryContainer else surfaceVariant)
                setTextColor(if (isSelected) onPrimaryContainer else onSurfaceVariant)

                setOnClickListener {
                    chatViewModel.selectModel(model)
                }
            }
            binding.chipGroupModels.addView(chip)
        }
    }

    private fun getThemeColor(attr: Int): Int {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
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

    private fun requestAudioFocus(): Boolean {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .build()
            audioFocusRequest = focusRequest
            audioManager?.requestAudioFocus(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(null)
        }
    }

    private fun speak(text: String) {
        requestAudioFocus()
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
        abandonAudioFocus()
        _binding = null
    }
}
