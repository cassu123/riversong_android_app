package com.riversongai.ui.viewmodel

import android.content.Context
import androidx.lifecycle.*
import com.riversongai.data.model.*
import com.riversongai.data.remote.RiverSongApiService
import com.riversongai.data.repository.FeedsRepository
import com.riversongai.data.repository.RoutinesRepository
import com.riversongai.data.repository.SmartHomeRepository
import com.riversongai.data.repository.UserRepository
import com.riversongai.utils.ErrorHandler
import com.riversongai.utils.SessionManager
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject

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

    private val _recentSessions = MutableLiveData<List<ChatSession>>(emptyList())
    val recentSessions: LiveData<List<ChatSession>> = _recentSessions

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _sessionExpired = MutableLiveData<Boolean>()
    val sessionExpired: LiveData<Boolean> = _sessionExpired

    // Widget visibility preferences
    private val _widgetVisibility = MutableLiveData<Map<String, Boolean>>()
    val widgetVisibility: LiveData<Map<String, Boolean>> = _widgetVisibility

    private val WIDGET_PREFS = "rs_dashboard_widgets"

    fun loadAllData() {
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
                val dashJob = async {
                    try {
                        val resp = apiService.getDashboard()
                        if (resp.isSuccessful) _dashboard.value = resp.body()
                    } catch (e: Exception) { /* non-fatal */ }
                }
                val weatherJob = async {
                    feedsRepository.getWeather()
                        .onSuccess { _weather.value = it }
                        .onFailure { _weather.value = null }
                }
                val routinesJob = async {
                    routinesRepository.getRoutines()
                        .onSuccess { _routines.value = it.filter { r -> r.isEnabled } }
                }
                val devicesJob = async {
                    smartHomeRepository.getAllDevices()
                        .onSuccess { _devices.value = it }
                }

                // Wait for all to complete in parallel
                userJob.await()
                dashJob.await()
                weatherJob.await()
                routinesJob.await()
                devicesJob.await()
                
                // Load local sessions
                loadLocalSessions()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadWidgetVisibility(context: Context) {
        val prefs = context.getSharedPreferences("rs_prefs", Context.MODE_PRIVATE)
        val json = prefs.getString(WIDGET_PREFS, null)
        val map = mutableMapOf(
            "health_status" to true,
            "system_status" to true,
            "recent_sessions" to true,
            "memory_activity" to true,
            "river_status" to true,
            "quick_actions" to true,
            "active_routines" to true,
            "weather" to true
        )
        if (json != null) {
            try {
                val obj = JSONObject(json)
                obj.keys().forEach { key -> map[key] = obj.getBoolean(key) }
            } catch (e: Exception) {}
        }
        _widgetVisibility.value = map
    }

    fun toggleWidget(context: Context, key: String) {
        val current = _widgetVisibility.value.orEmpty().toMutableMap()
        current[key] = !(current[key] ?: true)
        _widgetVisibility.value = current
        
        val prefs = context.getSharedPreferences("rs_prefs", Context.MODE_PRIVATE)
        val obj = JSONObject()
        current.forEach { (k, v) -> obj.put(k, v) }
        prefs.edit().putString(WIDGET_PREFS, obj.toString()).apply()
    }

    private fun loadLocalSessions() {
        // In the web app, this comes from localStorage.
        // For the thin-client Android app, we should ideally get this from the server
        // but if it's strictly local, we might need a dedicated endpoint or local storage logic.
        // Assuming we have a chat history endpoint for parity.
        viewModelScope.launch {
            try {
                val resp = apiService.getChatHistory()
                if (resp.isSuccessful) {
                    // Map ChatMessage to ChatSession summary if needed, or just use history
                    // For now, let's keep it empty or mock a few if history is available
                }
            } catch (e: Exception) {}
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
