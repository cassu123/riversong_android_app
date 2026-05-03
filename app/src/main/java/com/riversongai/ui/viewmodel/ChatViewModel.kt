package com.riversongai.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.riversongai.data.model.ChatMessage
import com.riversongai.data.repository.ConversationRepository
import com.riversongai.utils.AudioRecorder
import com.riversongai.utils.ChatHistoryManager
import com.riversongai.utils.Constants
import com.riversongai.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatViewModel(
    app: Application,
    private val conversationRepository: ConversationRepository
) : AndroidViewModel(app) {

    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _status = MutableLiveData("idle")
    val status: LiveData<String> = _status

    private val _isConnected = MutableLiveData(false)
    val isConnected: LiveData<Boolean> = _isConnected

    private val _connectionError = MutableLiveData<String?>()
    val connectionError: LiveData<String?> = _connectionError

    private val audioRecorder = AudioRecorder(app)
    val isRecording: Boolean get() = audioRecorder.isActive

    init {
        _messages.value = ChatHistoryManager.load(app)
        connect()
    }

    private fun connect() {
        conversationRepository.connect(
            baseUrl = Constants.BASE_URL,
            onMessage = { type, text -> handleServerMessage(type, text) },
            onConnected = {
                viewModelScope.launch(Dispatchers.Main) {
                    _isConnected.value = true
                    _connectionError.value = null
                }
            },
            onDisconnected = {
                viewModelScope.launch(Dispatchers.Main) {
                    _isConnected.value = false
                }
            },
            onError = { error ->
                viewModelScope.launch(Dispatchers.Main) {
                    _isConnected.value = false
                    _connectionError.value = error
                    _status.value = "idle"
                }
            }
        )
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage("user", text.trim())
        appendMessage(userMsg)
        _status.value = "thinking"
        if (!conversationRepository.isConnected()) connect()
        conversationRepository.sendText(text.trim())
        
        ChatHistoryManager.save(getApplication(), _messages.value.orEmpty())
    }

    @androidx.annotation.RequiresPermission(android.Manifest.permission.RECORD_AUDIO)
    fun startVoiceInput() {
        audioRecorder.start()
        _status.value = "listening"
    }

    fun stopVoiceInput() {
        _status.value = "transcribing"
        viewModelScope.launch(Dispatchers.IO) {
            @Suppress("MissingPermission")
            val base64Wav = audioRecorder.stopAndEncode()
            viewModelScope.launch(Dispatchers.Main) {
                if (base64Wav.isNotBlank()) {
                    appendMessage(ChatMessage("user", "🎤 Voice message"))
                    _status.value = "thinking"
                    if (!conversationRepository.isConnected()) connect()
                    conversationRepository.sendAudio(base64Wav)
                } else {
                    _status.value = "idle"
                }
            }
        }
    }

    fun cancelVoiceIfActive() {
        if (audioRecorder.isActive) {
            audioRecorder.cancel()
            _status.value = "idle"
        }
    }

    fun clearHistory() {
        _messages.value = emptyList()
        conversationRepository.resetHistory()
        ChatHistoryManager.save(getApplication(), emptyList())
    }

    fun reconnect() {
        conversationRepository.disconnect()
        connect()
    }

    private val _responseCompleteEvent = MutableLiveData<String?>()
    val responseCompleteEvent: LiveData<String?> = _responseCompleteEvent

    private fun handleServerMessage(type: String, text: String?) {
        viewModelScope.launch(Dispatchers.Main) {
            when (type) {
                "response_chunk" -> {
                    text ?: return@launch
                    val current = _messages.value.orEmpty().toMutableList()
                    val last = current.lastOrNull()
                    if (last?.isStreaming == true) {
                        current[current.lastIndex] = ChatMessage("assistant_streaming", last.content + text)
                    } else {
                        current.add(ChatMessage("assistant_streaming", text))
                    }
                    _messages.value = current
                }
                "response_complete" -> {
                    val fullText = text ?: return@launch
                    val current = _messages.value.orEmpty().toMutableList()
                    if (current.lastOrNull()?.isStreaming == true) {
                        current[current.lastIndex] = ChatMessage("assistant", fullText)
                    } else {
                        current.add(ChatMessage("assistant", fullText))
                    }
                    _messages.value = current
                    _status.value = "idle"
                    _responseCompleteEvent.value = fullText
                    
                    ChatHistoryManager.save(getApplication(), current)
                    NotificationHelper.showChatMessage(getApplication(), fullText)
                }
                "thinking" -> _status.value = "thinking"
                "transcribing" -> _status.value = "transcribing"
                "speaking" -> _status.value = "speaking"
                "idle" -> _status.value = "idle"
                "error" -> {
                    _status.value = "idle"
                    if (!text.isNullOrBlank()) appendMessage(ChatMessage("system", "⚠️ $text"))
                }
            }
        }
    }

    fun clearResponseCompleteEvent() {
        _responseCompleteEvent.value = null
    }

    private fun appendMessage(message: ChatMessage) {
        val current = _messages.value.orEmpty().toMutableList()
        current.add(message)
        _messages.value = current
    }

    override fun onCleared() {
        super.onCleared()
        audioRecorder.cancel()
        conversationRepository.disconnect()
    }
}
