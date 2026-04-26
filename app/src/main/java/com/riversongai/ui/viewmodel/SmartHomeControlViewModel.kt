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

    fun fetchDevices() {
        val token = sessionManager.getBearerToken() ?: return
        _isLoading.value = true
        viewModelScope.launch {
            smartHomeRepository.getAllDevices(token)
                .onSuccess {
                    _devices.value = it
                    _isLoading.value = false
                }
                .onFailure {
                    _errorMessage.value = ErrorHandler.getFriendlyMessage(it)
                    _isLoading.value = false
                }
        }
    }

    fun controlDevice(deviceId: String, command: String, value: String? = null) {
        val token = sessionManager.getBearerToken() ?: return
        viewModelScope.launch {
            smartHomeRepository.controlDevice(
                token,
                deviceId,
                DeviceControlRequest(command = command, value = value)
            ).onSuccess { updatedDevice ->
                // Replace the updated device in the list
                _devices.value = _devices.value?.map {
                    if (it.id == updatedDevice.id) updatedDevice else it
                }
            }.onFailure {
                _errorMessage.value = ErrorHandler.getFriendlyMessage(it)
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
