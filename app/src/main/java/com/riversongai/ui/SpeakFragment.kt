package com.riversongai.ui

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.pm.PackageManager
import android.media.*
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.riversongai.R
import com.riversongai.data.model.ChatMessage
import com.riversongai.databinding.FragmentSpeakBinding
import com.riversongai.ui.adapter.ChatAdapter
import com.riversongai.utils.SessionManager
import com.riversongai.utils.UIStyleManager
import com.riversongai.utils.WavEncoder
import kotlinx.coroutines.*
import okhttp3.*
import org.koin.android.ext.android.inject
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

import com.google.android.material.snackbar.Snackbar
import com.riversongai.utils.Constants

enum class SpeakState {
    CONNECTING, IDLE, LISTENING, TRANSCRIBING, THINKING, SPEAKING, ERROR
}

class SpeakFragment : Fragment(R.layout.fragment_speak) {

    private var _binding: FragmentSpeakBinding? = null
    private val binding get() = _binding!!

    private val sessionManager: SessionManager by inject()
    private val gson = Gson()
    private val okHttpClient = OkHttpClient()
    private var webSocket: WebSocket? = null
    
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    
    private var speakState = SpeakState.CONNECTING
    
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingJob: Job? = null
    
    private var audioTrack: AudioTrack? = null
    private val audioQueue = LinkedBlockingQueue<ByteArray>()
    private var playbackJob: Job? = null
    
    private var pulseAnimator: ObjectAnimator? = null

    private var retryCount = 0
    private val maxRetries = 3

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSpeakBinding.bind(view)

        setupRecyclerView()
        setupClickListeners()
        applyUIStyle()
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        binding.recyclerViewTranscript.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupClickListeners() {
        binding.buttonOrb.setOnClickListener {
            handleOrbClick()
        }
        binding.buttonReset.setOnClickListener {
            sendJson(mapOf("type" to "reset_history"))
            messages.clear()
            chatAdapter.submitList(emptyList())
        }
    }

    private fun handleOrbClick() {
        when (speakState) {
            SpeakState.IDLE -> startVoiceConversation()
            SpeakState.LISTENING -> stopVoiceConversation()
            SpeakState.ERROR -> checkPermissionAndConnect()
            else -> { /* Ignore in other states */ }
        }
    }

    private fun checkPermissionAndConnect() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 100)
            return
        }
        connectWebSocket()
    }

    private fun connectWebSocket() {
        val token = sessionManager.getAuthToken() ?: run {
            showError("No auth token found")
            return
        }
        val base = Constants.BASE_URL
            .replace("https://", "wss://")
            .replace("http://", "ws://")
            .trimEnd('/')
        val url = "${base}/ws/conversation?token=$token"
        
        val request = Request.Builder()
            .url(url)
            .build()
        
        updateState(SpeakState.CONNECTING)
        webSocket = okHttpClient.newWebSocket(request, webSocketListener)
    }

    private fun startVoiceInput() {
        // Renamed from startVoiceConversation for clarity or keeping the logic
        sendJson(mapOf("type" to "start"))
        startRecording()
    }

    private fun startVoiceConversation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 100)
            return
        }
        sendJson(mapOf("type" to "start"))
        startRecording()
    }

    private fun stopVoiceConversation() {
        sendJson(mapOf("type" to "stop"))
        stopRecording()
    }

    private fun startRecording() {
        val sampleRate = 16000
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)
        
        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            showError("Failed to initialize AudioRecord")
            return
        }

        audioRecord?.startRecording()
        isRecording = true
        
        recordingJob = lifecycleScope.launch(Dispatchers.IO) {
            val buffer = ByteArray(2048)
            while (isActive && isRecording) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    val pcm = buffer.copyOf(read)
                    val wav = WavEncoder.encode(pcm, sampleRate)
                    val base64 = Base64.encodeToString(wav, Base64.NO_WRAP)
                    sendJson(mapOf("type" to "audio_data", "data" to base64))
                }
                delay(100)
            }
        }
    }

    private fun stopRecording() {
        isRecording = false
        recordingJob?.cancel()
        try {
            audioRecord?.stop()
        } catch (e: Exception) {}
        audioRecord?.release()
        audioRecord = null
    }

    private fun playAudioChunk(base64: String) {
        val data = Base64.decode(base64, Base64.DEFAULT)
        // Skip WAV header (44 bytes) if present (RIFF header starts with 'R')
        val pcm = if (data.size > 44 && data[0] == 'R'.toByte()) {
            data.copyOfRange(44, data.size)
        } else {
            data
        }
        audioQueue.offer(pcm)
        
        if (playbackJob == null || !playbackJob!!.isActive) {
            startPlayback()
        }
    }

    private fun startPlayback() {
        playbackJob = lifecycleScope.launch(Dispatchers.IO) {
            val sampleRate = 16000
            val bufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
            audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM
            )
            audioTrack?.play()
            
            while (isActive) {
                val chunk = audioQueue.poll(1, TimeUnit.SECONDS) ?: break
                audioTrack?.write(chunk, 0, chunk.size)
            }
            
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        }
    }

    private fun updateState(state: SpeakState) {
        speakState = state
        lifecycleScope.launch(Dispatchers.Main) {
            binding.textViewStatusLabel.text = when (state) {
                SpeakState.CONNECTING -> "Connecting..."
                SpeakState.IDLE -> "Tap to speak"
                SpeakState.LISTENING -> "Listening..."
                SpeakState.TRANSCRIBING -> "Processing..."
                SpeakState.THINKING -> "River Song is thinking..."
                SpeakState.SPEAKING -> "River Song is speaking..."
                SpeakState.ERROR -> "Connection lost — tap to retry"
            }
            
            binding.imageOrbIcon.setImageResource(if (state == SpeakState.LISTENING) R.drawable.ic_close else R.drawable.ic_mic)
            
            if (state == SpeakState.LISTENING) {
                startPulseAnimation()
            } else {
                stopPulseAnimation()
            }
        }
    }

    private fun startPulseAnimation() {
        if (pulseAnimator == null) {
            val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.15f, 1.0f)
            val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.15f, 1.0f)
            val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0.1f, 0.4f, 0.1f)
            
            pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(binding.viewOrbPulse, scaleX, scaleY, alpha).apply {
                duration = 1500
                repeatCount = ObjectAnimator.INFINITE
                start()
            }
        }
    }

    private fun stopPulseAnimation() {
        pulseAnimator?.cancel()
        pulseAnimator = null
        binding.viewOrbPulse.scaleX = 1.0f
        binding.viewOrbPulse.scaleY = 1.0f
        binding.viewOrbPulse.alpha = 0f
    }

    private fun applyUIStyle() {
        val ctx = requireContext()
        binding.cardTranscript.setCardBackgroundColor(UIStyleManager.resolveCardColor(ctx, 2))
    }

    private fun addOrUpdateMessage(role: String, content: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            val lastMsg = messages.lastOrNull()
            if (role == "assistant_streaming") {
                if (lastMsg != null && lastMsg.role == "assistant_streaming") {
                    val updatedMsg = lastMsg.copy(content = lastMsg.content + content)
                    messages[messages.size - 1] = updatedMsg
                } else {
                    messages.add(ChatMessage("assistant_streaming", content))
                }
            } else if (role == "assistant") {
                if (lastMsg != null && lastMsg.role == "assistant_streaming") {
                    messages[messages.size - 1] = ChatMessage("assistant", content)
                } else {
                    messages.add(ChatMessage("assistant", content))
                }
            } else {
                messages.add(ChatMessage(role, content))
            }
            chatAdapter.submitList(messages.toList())
            if (messages.isNotEmpty()) {
                binding.recyclerViewTranscript.smoothScrollToPosition(messages.size - 1)
            }
        }
    }

    private fun appendAiMessage(chunk: String) {
        addOrUpdateMessage("assistant_streaming", chunk)
    }

    private fun sendJson(data: Any) {
        val json = gson.toJson(data)
        webSocket?.send(json)
    }

    private fun showError(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            updateState(SpeakState.ERROR)
        }
    }

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            retryCount = 0
            updateState(SpeakState.IDLE)
            sendJson(mapOf("type" to "ambient_mode", "enabled" to false))
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val resp = gson.fromJson(text, Map::class.java)
            val type = resp["type"] as? String ?: return
            
            when (type) {
                "connected" -> {
                    retryCount = 0
                    updateState(SpeakState.IDLE)
                }
                "listening" -> updateState(SpeakState.LISTENING)
                "transcribing" -> updateState(SpeakState.TRANSCRIBING)
                "transcript" -> (resp["text"] as? String)?.let { addOrUpdateMessage("user", it) }
                "thinking" -> updateState(SpeakState.THINKING)
                "response_chunk" -> (resp["text"] as? String)?.let { appendAiMessage(it) }
                "stream_done" -> { /* Handled by idle or response_complete */ }
                "response_complete" -> (resp["text"] as? String)?.let { addOrUpdateMessage("assistant", it) }
                "speaking" -> updateState(SpeakState.SPEAKING)
                "audio" -> (resp["data"] as? String)?.let { playAudioChunk(it) }
                "idle" -> updateState(SpeakState.IDLE)
                "error" -> showError(resp["message"] as? String ?: "Unknown error")
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            updateState(SpeakState.ERROR)
            if (retryCount < maxRetries) {
                retryCount++
                lifecycleScope.launch {
                    delay(2000)
                    if (isAdded) checkPermissionAndConnect()
                }
            } else {
                showError("Could not connect. Please check your connection.")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissionAndConnect()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                connectWebSocket()
            } else {
                Snackbar.make(binding.root, "Microphone permission is required for voice conversation.", Snackbar.LENGTH_LONG).show()
                updateState(SpeakState.ERROR)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopVoiceConversation()
        webSocket?.close(1000, "Fragment paused")
        webSocket = null
        playbackJob?.cancel()
        audioQueue.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
