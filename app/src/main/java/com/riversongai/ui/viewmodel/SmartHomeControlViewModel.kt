package com.riversongai.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riversongai.data.model.Device
import com.riversongai.data.repository.SmartHomeRepository
import com.riversongai.utils.ErrorHandler
import com.riversongai.utils.SessionManager
import kotlinx.coroutines.launch

class SmartHomeControlViewModel(
    private val smartHomeRepository: SmartHomeRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _devices = MutableLiveData<List<Device>>()
    val devices: LiveData<List<Device>> = _devices

    private val _status = MutableLiveData<com.riversongai.data.model.HomeStatus?>()
    val status: LiveData<com.riversongai.data.model.HomeStatus?> = _status

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _sessionExpired = MutableLiveData<Boolean>()
    val sessionExpired: LiveData<Boolean> = _sessionExpired

    fun fetchStatus() {
        viewModelScope.launch {
            smartHomeRepository.getHomeStatus()
                .onSuccess { _status.value = it }
        }
    }

    fun fetchDevices() {
        if (!sessionManager.isLoggedIn()) {
            _sessionExpired.value = true
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            smartHomeRepository.getAllDevices()
                .onSuccess {
                    _devices.value = it
                    _isLoading.value = false
                }
                .onFailure {
                    handleError(it)
                    _isLoading.value = false
                }
        }
    }

    fun controlDevice(entityId: String, action: String, brightnessPct: Int? = null) {
        viewModelScope.launch {
            smartHomeRepository.controlDevice(entityId, action, brightnessPct)
                .onSuccess {
                    // Optimistically update the local state
                    _devices.value = _devices.value?.map { d ->
                        if (d.entityId == entityId) {
                            val newState = when (action) {
                                "turn_on" -> "on"
                                "turn_off" -> "off"
                                "toggle" -> if (d.isOn) "off" else "on"
                                else -> d.state
                            }
                            d.copy(state = newState)
                        } else d
                    }
                }
                .onFailure { handleError(it) }
        }
    }

    fun clearError() { _errorMessage.value = null }

    private fun handleError(e: Throwable) {
        if (e is retrofit2.HttpException && e.code() == 401) {
            sessionManager.clearSession()
            _sessionExpired.value = true
        } else {
            _errorMessage.value = ErrorHandler.getFriendlyMessage(e)
        }
    }
}
