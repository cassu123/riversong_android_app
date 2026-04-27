package com.riversongai.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riversongai.data.model.ActivitySummary
import com.riversongai.data.model.SmartHomeSummary
import com.riversongai.data.model.User
import com.riversongai.data.repository.SmartHomeRepository
import com.riversongai.data.repository.UserRepository
import com.riversongai.utils.ErrorHandler
import com.riversongai.utils.SessionManager
import kotlinx.coroutines.launch

class UserDashboardViewModel(
    private val userRepository: UserRepository,
    private val smartHomeRepository: SmartHomeRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _smartHomeSummary = MutableLiveData<SmartHomeSummary?>()
    val smartHomeSummary: LiveData<SmartHomeSummary?> = _smartHomeSummary

    private val _activitySummary = MutableLiveData<ActivitySummary?>()
    val activitySummary: LiveData<ActivitySummary?> = _activitySummary

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _sessionExpired = MutableLiveData<Boolean>()
    val sessionExpired: LiveData<Boolean> = _sessionExpired

    fun loadDashboardData() {
        _isLoading.value = true
        viewModelScope.launch {
            userRepository.getCurrentUser()
                .onSuccess { _currentUser.value = it }
                .onFailure { handleError(it) }

            smartHomeRepository.getAllDevices()
                .onSuccess { devices ->
                    val active = devices.count { it.status == "online" || it.isOn == true }
                    val offline = devices.count { it.status == "offline" }
                    _smartHomeSummary.value = SmartHomeSummary(
                        totalDevices = devices.size,
                        activeDevices = active,
                        offlineDevices = offline
                    )
                }
                .onFailure { handleError(it) }

            _activitySummary.value = ActivitySummary()
            _isLoading.value = false
        }
    }

    fun logout() {
        sessionManager.clearSession()
        _sessionExpired.value = true
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
