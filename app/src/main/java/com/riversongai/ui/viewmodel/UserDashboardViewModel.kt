package com.riversongai.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riversongai.data.model.ActivitySummary
import com.riversongai.data.model.SmartHomeSummary
import com.riversongai.data.model.User
import com.riversongai.data.repository.MemoryRepository
import com.riversongai.data.repository.RoutinesRepository
import com.riversongai.data.repository.SmartHomeRepository
import com.riversongai.data.repository.UserRepository
import com.riversongai.utils.ErrorHandler
import com.riversongai.utils.SessionManager
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class UserDashboardViewModel(
    private val userRepository: UserRepository,
    private val smartHomeRepository: SmartHomeRepository,
    private val memoryRepository: MemoryRepository,
    private val routinesRepository: RoutinesRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _factsCount = MutableLiveData<Int>(0)
    val factsCount: LiveData<Int> = _factsCount

    private val _routinesCount = MutableLiveData<Int>(0)
    val routinesCount: LiveData<Int> = _routinesCount

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
        if (!sessionManager.isLoggedIn()) {
            _sessionExpired.value = true
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val userJob = async {
                    userRepository.getCurrentUser()
                        .onSuccess { _currentUser.value = it }
                        .onFailure { handleError(it) }
                }

                val devicesJob = async {
                    smartHomeRepository.getAllDevices()
                        .onSuccess { devices ->
                            val active = devices.count { it.isOn }
                            val offline = devices.count { it.state == "unavailable" }
                            _smartHomeSummary.value = SmartHomeSummary(
                                totalDevices = devices.size,
                                activeDevices = active,
                                offlineDevices = offline
                            )
                        }
                        .onFailure {
                            _smartHomeSummary.value = SmartHomeSummary(0, 0, 0)
                        }
                }

                val memoryJob = async {
                    memoryRepository.getFacts()
                        .onSuccess { _factsCount.value = it.size }
                }

                val routinesJob = async {
                    routinesRepository.getRoutines()
                        .onSuccess { _routinesCount.value = it.size }
                }

                userJob.await()
                devicesJob.await()
                memoryJob.await()
                routinesJob.await()
                
                _activitySummary.value = ActivitySummary()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        sessionManager.clearSession()
        _sessionExpired.value = true
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
