package com.riversongai.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riversongai.data.model.*
import com.riversongai.data.remote.RiverSongApiService
import kotlinx.coroutines.launch

class UserDashboardViewModel(
    private val apiService: RiverSongApiService,
    private val sessionManager: com.riversongai.utils.SessionManager
) : ViewModel() {

    private val _currentUser = MutableLiveData<UserProfile?>()
    val currentUser: LiveData<UserProfile?> = _currentUser

    private val _integrations = MutableLiveData<Integrations?>()
    val integrations: LiveData<Integrations?> = _integrations

    private val _factsCount = MutableLiveData<Int>()
    val factsCount: LiveData<Int> = _factsCount

    private val _routinesCount = MutableLiveData<Int>()
    val routinesCount: LiveData<Int> = _routinesCount

    private val _smartHomeSummary = MutableLiveData<SmartHomeSummary>()
    val smartHomeSummary: LiveData<SmartHomeSummary> = _smartHomeSummary

    private val _profileUpdateResult = MutableLiveData<String?>()
    val profileUpdateResult: LiveData<String?> = _profileUpdateResult

    private val _passwordChangeResult = MutableLiveData<String?>()
    val passwordChangeResult: LiveData<String?> = _passwordChangeResult

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _sessionExpired = MutableLiveData<Boolean>()
    val sessionExpired: LiveData<Boolean> = _sessionExpired

    fun loadDashboardData() {
        viewModelScope.launch {
            try {
                val profileResp = apiService.getCurrentUser()
                if (profileResp.isSuccessful) _currentUser.value = profileResp.body()

                val dashResp = apiService.getDashboard()
                if (dashResp.isSuccessful) {
                    val stats = dashResp.body()
                    _factsCount.value = stats?.memory?.facts ?: 0
                }

                val routinesResp = apiService.getOrchestrationSettings() // Mocking routines count or loading properly if endpoint exists
                
                loadIntegrations()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun loadIntegrations() {
        viewModelScope.launch {
            try {
                val resp = apiService.getIntegrations()
                if (resp.isSuccessful) _integrations.value = resp.body()
            } catch (e: Exception) {}
        }
    }

    fun updateProfile(first: String, last: String, username: String?, birthday: String?) {
        viewModelScope.launch {
            try {
                val body = UserProfileUpdate(
                    displayName = "$first $last".trim(),
                    username = username,
                    birthday = birthday
                )
                val resp = apiService.updateUserProfile(body)
                if (resp.isSuccessful) {
                    _currentUser.value = resp.body()
                    _profileUpdateResult.value = "Profile updated successfully"
                } else {
                    _errorMessage.value = "Update failed: ${resp.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun saveIntegrations(integrations: Integrations) {
        viewModelScope.launch {
            try {
                val resp = apiService.saveIntegrations(integrations)
                if (resp.isSuccessful) {
                    _integrations.value = integrations
                    _profileUpdateResult.value = "Integrations saved"
                }
            } catch (e: Exception) { _errorMessage.value = e.message }
        }
    }

    fun changePassword(current: String, next: String) {
        viewModelScope.launch {
            try {
                val resp = apiService.changePassword(mapOf("current_password" to current, "new_password" to next))
                if (resp.isSuccessful) {
                    _passwordChangeResult.value = "Password updated successfully"
                } else {
                    _errorMessage.value = "Failed to update password"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun logout() {
        sessionManager.clearSession()
        _sessionExpired.value = true
    }

    fun clearError() { _errorMessage.value = null }
}

data class SmartHomeSummary(
    val activeDevices: Int = 0,
    val offlineDevices: Int = 0,
    val totalDevices: Int = 0
)
