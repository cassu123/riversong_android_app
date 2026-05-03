package com.riversongai.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riversongai.data.model.LlmSettings
import com.riversongai.data.model.ModelCatalog
import com.riversongai.data.model.MemoryTtlSettings
import com.riversongai.data.model.VoiceOption
import com.riversongai.data.repository.SettingsRepository
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    private val _modelCatalog = MutableLiveData<ModelCatalog?>()
    val modelCatalog: LiveData<ModelCatalog?> = _modelCatalog

    private val _llmSettings = MutableLiveData<LlmSettings?>()
    val llmSettings: LiveData<LlmSettings?> = _llmSettings

    private val _voices = MutableLiveData<List<VoiceOption>>(emptyList())
    val voices: LiveData<List<VoiceOption>> = _voices

    private val _selectedVoice = MutableLiveData<VoiceOption?>(null)
    val selectedVoice: LiveData<VoiceOption?> = _selectedVoice

    private val _memoryTtl = MutableLiveData<MemoryTtlSettings?>()
    val memoryTtl: LiveData<MemoryTtlSettings?> = _memoryTtl

    private val _voicePreviewData = MutableLiveData<ByteArray?>()
    val voicePreviewData: LiveData<ByteArray?> = _voicePreviewData

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _saveResult = MutableLiveData<String?>()
    val saveResult: LiveData<String?> = _saveResult

    private val _connectionTestResult = MutableLiveData<String?>()
    val connectionTestResult: LiveData<String?> = _connectionTestResult

    init {
        loadSettings()
    }

    fun loadVoices() {
        viewModelScope.launch {
            settingsRepository.getVoices().onSuccess { _voices.value = it }
        }
    }

    fun loadMemoryTtl() {
        viewModelScope.launch {
            settingsRepository.getMemoryTtl().onSuccess { _memoryTtl.value = it }
        }
    }

    fun saveMemoryTtl(settings: MemoryTtlSettings) {
        viewModelScope.launch {
            _isLoading.value = true
            settingsRepository.saveMemoryTtl(settings).fold(
                onSuccess = {
                    _memoryTtl.value = it
                    _saveResult.value = "Memory TTL saved"
                },
                onFailure = { _error.value = it.message }
            )
            _isLoading.value = false
        }
    }

    fun selectVoice(voice: VoiceOption) {
        _selectedVoice.value = voice
    }

    fun testVoice(voiceId: String) {
        viewModelScope.launch {
            settingsRepository.testVoice(voiceId).onSuccess {
                _voicePreviewData.value = it
            }
        }
    }

    fun clearVoicePreviewData() { _voicePreviewData.value = null }

    fun loadSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            settingsRepository.getModels().fold(
                onSuccess = { _modelCatalog.value = it },
                onFailure = { _error.value = it.message }
            )

            settingsRepository.getLlmSettings().fold(
                onSuccess = { _llmSettings.value = it },
                onFailure = { /* non-fatal */ }
            )

            _isLoading.value = false
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _isLoading.value = true
            settingsRepository.testConnection().fold(
                onSuccess = { stats ->
                    _connectionTestResult.value = "Connected — uptime ${stats.uptimeSeconds}s"
                },
                onFailure = { _error.value = it.message }
            )
            _isLoading.value = false
        }
    }

    fun saveModel(provider: String, modelId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            settingsRepository.saveLlmSettings(provider, modelId).fold(
                onSuccess = {
                    _saveResult.value = "Model updated to $modelId"
                    loadSettings()
                },
                onFailure = { _error.value = it.message }
            )
            _isLoading.value = false
        }
    }

    fun clearSaveResult() { _saveResult.value = null }
    fun clearError() { _error.value = null }
    fun clearConnectionTestResult() { _connectionTestResult.value = null }
}
