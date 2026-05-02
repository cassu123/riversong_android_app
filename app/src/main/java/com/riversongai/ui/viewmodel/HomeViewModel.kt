package com.riversongai.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riversongai.data.model.Device
import com.riversongai.data.model.User
import com.riversongai.data.repository.SmartHomeRepository
import com.riversongai.data.repository.UserRepository
import com.riversongai.utils.ErrorHandler
import com.riversongai.utils.SessionManager
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userRepository: UserRepository,
    private val smartHomeRepository: SmartHomeRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _devices = MutableLiveData<List<Device>>()
    val devices: LiveData<List<Device>> = _devices

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _sessionExpired = MutableLiveData<Boolean>()
    val sessionExpired: LiveData<Boolean> = _sessionExpired

    fun loadUserDataAndDevices() {
        if (!sessionManager.isLoggedIn()) {
            _sessionExpired.value = true
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            try {
                userRepository.getCurrentUser()
                    .onSuccess { _currentUser.value = it }
                    .onFailure { handleError(it) }

                smartHomeRepository.getAllDevices()
                    .onSuccess { _devices.value = it }
                    .onFailure { /* non-fatal — HA may not be configured */ }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleAllLights(on: Boolean) {
        val lights = _devices.value?.filter { it.domain == "light" } ?: return
        viewModelScope.launch {
            lights.forEach { device ->
                smartHomeRepository.controlDevice(
                    device.entityId,
                    if (on) "turn_on" else "turn_off"
                )
            }
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
