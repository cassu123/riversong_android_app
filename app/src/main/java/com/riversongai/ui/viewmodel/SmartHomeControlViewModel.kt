package com.riversongai.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riversongai.data.model.Device
import com.riversongai.data.remote.DeviceControlRequest
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

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _sessionExpired = MutableLiveData<Boolean>()
    val sessionExpired: LiveData<Boolean> = _sessionExpired

    fun fetchDevices() {
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

    fun controlDevice(deviceId: String, command: String, value: String? = null) {
        viewModelScope.launch {
            smartHomeRepository.controlDevice(
                deviceId,
                DeviceControlRequest(command = command, value = value)
            ).onSuccess { updatedDevice ->
                _devices.value = _devices.value?.map {
                    if (it.id == updatedDevice.id) updatedDevice else it
                }
            }.onFailure { handleError(it) }
        }
    }

    fun clearError() { _errorMessage.value = null }

    private fun handleError(e: Throwable) {
        if (e.message?.contains("401") == true) {
            sessionManager.clearSession()
            _sessionExpired.value = true
        } else {
            _errorMessage.value = ErrorHandler.getFriendlyMessage(e)
        }
    }
}
