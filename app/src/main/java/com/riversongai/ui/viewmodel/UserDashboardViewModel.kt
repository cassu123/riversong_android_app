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

    fun loadDashboardData() {
        val token = sessionManager.getBearerToken() ?: return
        _isLoading.value = true
        viewModelScope.launch {
            userRepository.getCurrentUser(token)
                .onSuccess { _currentUser.value = it }
                .onFailure { _errorMessage.value = ErrorHandler.getFriendlyMessage(it) }

            smartHomeRepository.getAllDevices(token)
                .onSuccess { devices ->
                    val active = devices.count { it.status == "online" || it.isOn == true }
                    val offline = devices.count { it.status == "offline" }
                    _smartHomeSummary.value = SmartHomeSummary(
                        totalDevices = devices.size,
                        activeDevices = active,
                        offlineDevices = offline
                    )
                }
                .onFailure { _errorMessage.value = ErrorHandler.getFriendlyMessage(it) }

            // Activity data will come from the backend once that module is ready
            _activitySummary.value = ActivitySummary()

            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
