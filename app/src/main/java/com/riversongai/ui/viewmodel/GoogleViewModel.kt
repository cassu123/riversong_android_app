package com.riversongai.ui.viewmodel

import androidx.lifecycle.*
import com.riversongai.data.model.*
import com.riversongai.data.remote.RiverSongApiService
import kotlinx.coroutines.launch

class GoogleViewModel(private val apiService: RiverSongApiService) : ViewModel() {

    private val _status = MutableLiveData<GoogleStatus?>()
    val status: LiveData<GoogleStatus?> = _status

    private val _events = MutableLiveData<List<CalendarEvent>>()
    val events: LiveData<List<CalendarEvent>> = _events

    private val _messages = MutableLiveData<List<GmailMessage>>()
    val messages: LiveData<List<GmailMessage>> = _messages

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _authUrl = MutableLiveData<String?>()
    val authUrl: LiveData<String?> = _authUrl

    fun loadAll() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val statusResp = apiService.getGoogleStatus()
                if (statusResp.isSuccessful) {
                    val s = statusResp.body()
                    _status.value = s
                    if (s?.connected == true) {
                        loadDetails()
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadDetails() {
        try {
            val calendarResp = apiService.getCalendarEvents()
            if (calendarResp.isSuccessful) {
                _events.value = calendarResp.body()?.events ?: emptyList()
            }

            val gmailResp = apiService.getGmailUnread()
            if (gmailResp.isSuccessful) {
                _messages.value = gmailResp.body()?.messages ?: emptyList()
            }
        } catch (e: Exception) {
            // non-fatal
        }
    }

    fun fetchAuthUrl() {
        viewModelScope.launch {
            try {
                val resp = apiService.getGoogleAuthUrl("https://riversongai.com/callback")
                if (resp.isSuccessful) {
                    _authUrl.value = resp.body()?.authUrl
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearAuthUrl() { _authUrl.value = null }
    fun clearError() { _error.value = null }
}
