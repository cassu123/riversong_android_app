package com.riversongai.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riversongai.data.model.ChatMessage
import com.riversongai.data.model.ChatRequest
import com.riversongai.data.model.ModelEntry
import com.riversongai.data.remote.RiverSongApiService
import com.riversongai.data.repository.ConversationRepository
import com.riversongai.data.repository.SettingsRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ChatViewModel(
    private val conversationRepository: ConversationRepository,
    private val settingsRepository: SettingsRepository,
    private val apiService: RiverSongApiService
) : ViewModel() {

    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _streamingResponse = MutableLiveData<String>("")
    val streamingResponse: LiveData<String> = _streamingResponse

    private val _isThinking = MutableLiveData(false)
    val isThinking: LiveData<Boolean> = _isThinking

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _models = MutableLiveData<List<ModelEntry>>(emptyList())
    val models: LiveData<List<ModelEntry>> = _models

    private val _selectedModel = MutableLiveData<ModelEntry?>()
    val selectedModel: LiveData<ModelEntry?> = _selectedModel

    init {
        loadModels()
    }

    private fun loadModels() {
        viewModelScope.launch {
            settingsRepository.getModelCatalog().onSuccess { catalog ->
                _models.value = catalog.local + catalog.cloud
                loadCurrentSettings()
            }
        }
    }

    private fun loadCurrentSettings() {
        viewModelScope.launch {
            settingsRepository.getLlmSettings().onSuccess { settings ->
                _selectedModel.value = _models.value?.find { it.modelId == settings.model }
            }
        }
    }

    fun selectModel(model: ModelEntry) {
        _selectedModel.value = model
        viewModelScope.launch {
            settingsRepository.saveLlmSettings(model.provider, model.modelId)
        }
    }

    fun sendMessage(text: String) {
        val t = text.trim()
        if (t.isEmpty()) return

        val currentMessages = _messages.value.orEmpty().toMutableList()
        currentMessages.add(ChatMessage(role = "user", content = t))
        _messages.value = currentMessages

        _isThinking.value = true
        _streamingResponse.value = ""

        viewModelScope.launch {
            try {
                val history = currentMessages.takeLast(21) // system + 20 context
                val request = ChatRequest(
                    message = t,
                    history = history.map { mapOf("role" to it.role, "content" to it.content) },
                    provider = _selectedModel.value?.provider,
                    model = _selectedModel.value?.modelId
                )

                // Use the new SSE-style streaming from the conversation repository
                // (I need to ensure ConversationRepository supports Flow/Streaming)
                conversationRepository.streamChat(request).collect { chunk ->
                    if (chunk == "[DONE]") {
                        val finalResponse = _streamingResponse.value ?: ""
                        currentMessages.add(ChatMessage(role = "assistant", content = finalResponse))
                        _messages.value = currentMessages
                        _streamingResponse.value = ""
                        _isThinking.value = false
                        extractFacts(currentMessages)
                    } else if (chunk.startsWith("[ERROR]")) {
                        _error.value = chunk.removePrefix("[ERROR] ")
                        _isThinking.value = false
                    } else {
                        _streamingResponse.value = (_streamingResponse.value ?: "") + chunk
                    }
                }
            } catch (e: Exception) {
                _error.value = "Connection lost: ${e.message}"
                _isThinking.value = false
            }
        }
    }

    private fun extractFacts(msgs: List<ChatMessage>) {
        if (msgs.size < 2) return
        viewModelScope.launch {
            try {
                apiService.extractFacts(mapOf("messages" to msgs.map { mapOf("role" to it.role, "content" to it.content) }))
            } catch (e: Exception) {}
        }
    }

    fun resetChat() {
        _messages.value = emptyList()
        _streamingResponse.value = ""
        _error.value = null
    }

    fun clearError() { _error.value = null }
}

// Ensure ConversationRepository has streamChat
