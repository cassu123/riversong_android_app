package com.riversongai.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.riversongai.R
import com.riversongai.databinding.FragmentSpeakBinding
import com.riversongai.ui.viewmodel.ChatViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SpeakFragment : Fragment(R.layout.fragment_speak) {

    private var _binding: FragmentSpeakBinding? = null
    private val binding get() = _binding!!
    private val chatViewModel: ChatViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSpeakBinding.bind(view)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.buttonMic.setOnClickListener {
            if (chatViewModel.isRecording) {
                chatViewModel.stopVoiceInput()
            } else {
                chatViewModel.startVoiceInput()
            }
        }

        binding.buttonReset.setOnClickListener {
            // Placeholder for reset logic
        }
    }

    private fun observeViewModel() {
        chatViewModel.status.observe(viewLifecycleOwner) { status ->
            binding.textViewStatus.text = status.uppercase()
            
            // Holographic animation placeholder logic
            when (status) {
                "listening" -> {
                    binding.buttonMic.setImageResource(R.drawable.ic_close)
                    binding.textViewTranscript.text = "I'm listening..."
                }
                "thinking" -> {
                    binding.textViewTranscript.text = "Working on it..."
                }
                "speaking" -> {
                    binding.textViewTranscript.text = "The garage door has been closed..."
                }
                else -> {
                    binding.buttonMic.setImageResource(R.drawable.ic_mic)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
