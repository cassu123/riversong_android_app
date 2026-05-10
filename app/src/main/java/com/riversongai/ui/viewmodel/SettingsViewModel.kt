package com.riversongai.ui.viewmodel

import androidx.lifecycle.*
import com.riversongai.data.model.*
import com.riversongai.data.remote.RiverSongApiService
import com.riversongai.data.repository.SettingsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val apiService: RiverSongApiService
) : ViewModel() {

    private val _modelCatalog = MutableLiveData<ModelCatalog?>()
    val modelCatalog: LiveData<ModelCatalog?> = _modelCatalog

    private val _llmSettings = MutableLiveData<LlmSettings?>()
    val llmSettings: LiveData<LlmSettings?> = _llmSettings

    private val _voices = MutableLiveData<List<VoiceOption>>(emptyList())
    val voices: LiveData<List<VoiceOption>> = _voices

    private val _selectedVoice = MutableLiveData<VoiceOption?>()
    val selectedVoice: LiveData<VoiceOption?> = _selectedVoice

    private val _memoryTtl = MutableLiveData<MemoryTtlSettings?>()
    val memoryTtl: LiveData<MemoryTtlSettings?> = _memoryTtl

    private val _n8nSettings = MutableLiveData<N8nSettings?>()
    val n8nSettings: LiveData<N8nSettings?> = _n8nSettings

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

    fun loadSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            val catalogJob = async { settingsRepository.getModelCatalog() }
            val llmJob     = async { settingsRepository.getLlmSettings() }
            val voiceJob   = async { apiService.getVoiceSettings() }
            val memoryJob  = async { apiService.getMemorySettings() }
            val n8nJob     = async { apiService.getOrchestrationSettings() }

            catalogJob.await().onSuccess { _modelCatalog.value = it }
            llmJob.await().onSuccess { _llmSettings.value = it }
            
            val vResp = voiceJob.await()
            if (vResp.isSuccessful) {
                val list = vResp.body() ?: emptyList()
                _voices.value = list
                _selectedVoice.value = list.find { it.active }
            }

            val mResp = memoryJob.await()
            if (mResp.isSuccessful) _memoryTtl.value = mResp.body()

            val nResp = n8nJob.await()
            if (nResp.isSuccessful) _n8nSettings.value = nResp.body()

            _isLoading.value = false
        }
    }

    fun saveModel(
        provider: String,
        modelId: String,
        fallbackEnabled: Boolean? = null,
        fallbackProvider: String? = null,
        fallbackModel: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            settingsRepository.saveLlmSettings(provider, modelId, fallbackEnabled, fallbackProvider, fallbackModel).fold(
                onSuccess = {
                    _saveResult.value = "Settings updated"
                    loadSettings()
                },
                onFailure = { _error.value = it.message }
            )
            _isLoading.value = false
        }
    }

    fun selectVoice(voice: VoiceOption) {
        viewModelScope.launch {
            val resp = apiService.setVoice(mapOf("voice_id" to voice.id))
            if (resp.isSuccessful) {
                _selectedVoice.value = voice
                _saveResult.value = "Voice changed to ${voice.name}"
            }
        }
    }

    fun testVoice(voiceId: String) {
        viewModelScope.launch {
            try {
                val resp = apiService.previewVoice(voiceId)
                if (resp.isSuccessful) {
                    val b64 = resp.body()?.get("audio_b64")
                    if (b64 != null) {
                        _voicePreviewData.value = android.util.Base64.decode(b64, android.util.Base64.DEFAULT)
                    }
                }
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun clearVoicePreviewData() { _voicePreviewData.value = null }

    fun saveMemoryTtl(settings: MemoryTtlSettings) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val resp = apiService.saveMemorySettings(settings)
                if (resp.isSuccessful) {
                    _memoryTtl.value = settings
                    _saveResult.value = "Retention updated"
                }
            } catch (e: Exception) { _error.value = e.message }
            finally { _isLoading.value = false }
        }
    }

    fun saveN8nSettings(s: N8nSettings) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val resp = apiService.saveOrchestrationSettings(s)
                if (resp.isSuccessful) {
                    _n8nSettings.value = s
                    _saveResult.value = "Orchestration saved"
                }
            } catch (e: Exception) { _error.value = e.message }
            finally { _isLoading.value = false }
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _isLoading.value = true
            settingsRepository.testConnection().fold(
                onSuccess = { _connectionTestResult.value = it },
                onFailure = { _error.value = it.message }
            )
            _isLoading.value = false
        }
    }

    fun getApiService() = apiService

    fun clearSaveResult() { _saveResult.value = null }
    fun clearError() { _error.value = null }
    fun clearConnectionTestResult() { _connectionTestResult.value = null }
}
