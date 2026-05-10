package com.riversongai.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riversongai.data.model.DashboardStats
import com.riversongai.data.model.Device
import com.riversongai.data.model.Routine
import com.riversongai.data.model.User
import com.riversongai.data.model.WeatherData
import com.riversongai.data.remote.RiverSongApiService
import com.riversongai.data.repository.FeedsRepository
import com.riversongai.data.repository.RoutinesRepository
import com.riversongai.data.repository.SmartHomeRepository
import com.riversongai.data.repository.UserRepository
import com.riversongai.utils.ErrorHandler
import com.riversongai.utils.SessionManager
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userRepository: UserRepository,
    private val smartHomeRepository: SmartHomeRepository,
    private val feedsRepository: FeedsRepository,
    private val routinesRepository: RoutinesRepository,
    private val sessionManager: SessionManager,
    private val apiService: RiverSongApiService
) : ViewModel() {

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _devices = MutableLiveData<List<Device>>()
    val devices: LiveData<List<Device>> = _devices

    private val _weather = MutableLiveData<WeatherData?>()
    val weather: LiveData<WeatherData?> = _weather

    private val _dashboard = MutableLiveData<DashboardStats?>()
    val dashboard: LiveData<DashboardStats?> = _dashboard

    private val _routines = MutableLiveData<List<Routine>>()
    val routines: LiveData<List<Routine>> = _routines

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _sessionExpired = MutableLiveData<Boolean>()
    val sessionExpired: LiveData<Boolean> = _sessionExpired

    fun loadAllData() {
        if (!sessionManager.isLoggedIn()) {
            _sessionExpired.value = true
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // User Data
                userRepository.getCurrentUser()
                    .onSuccess { _currentUser.value = it }
                    .onFailure { handleError(it) }

                // Dashboard Stats
                try {
                    val resp = apiService.getDashboard()
                    if (resp.isSuccessful) _dashboard.value = resp.body()
                } catch (e: Exception) { /* non-fatal */ }

                // Weather
                feedsRepository.getWeather()
                    .onSuccess { _weather.value = it }
                    .onFailure { _weather.value = null }

                // Routines
                routinesRepository.getRoutines()
                    .onSuccess { _routines.value = it.filter { r -> r.isEnabled } }

                // Smart Home
                smartHomeRepository.getAllDevices()
                    .onSuccess { _devices.value = it }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUserDataAndDevices() = loadAllData()

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
