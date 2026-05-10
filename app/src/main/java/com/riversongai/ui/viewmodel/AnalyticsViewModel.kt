package com.riversongai.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riversongai.data.model.AnalyticsSnapshot
import com.riversongai.data.model.PlatformSummary
import com.riversongai.data.remote.RiverSongApiService
import kotlinx.coroutines.launch

class AnalyticsViewModel(private val apiService: RiverSongApiService) : ViewModel() {

    private val _snapshots = MutableLiveData<List<AnalyticsSnapshot>>()
    val snapshots: LiveData<List<AnalyticsSnapshot>> = _snapshots

    private val _platforms = MutableLiveData<List<String>>()
    val platforms: LiveData<List<String>> = _platforms

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _platformSummaries = MutableLiveData<Map<String, String>>(emptyMap())
    val platformSummaries: LiveData<Map<String, String>> = _platformSummaries

    var currentDays = 30

    fun loadData(days: Int = currentDays) {
        currentDays = days
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val snapshotsResp = apiService.getAnalyticsSnapshots(days)
                if (snapshotsResp.isSuccessful) {
                    _snapshots.value = snapshotsResp.body() ?: emptyList()
                } else {
                    _error.value = "Failed to load snapshots: ${snapshotsResp.code()}"
                }

                val platformsResp = apiService.getAnalyticsPlatforms()
                if (platformsResp.isSuccessful) {
                    _platforms.value = platformsResp.body() ?: emptyList()
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadPlatformSummary(platform: String) {
        viewModelScope.launch {
            try {
                val resp = apiService.getPlatformSummary(platform)
                if (resp.isSuccessful && resp.body() != null) {
                    val current = _platformSummaries.value.orEmpty().toMutableMap()
                    current[platform] = resp.body()!!.summary
                    _platformSummaries.value = current
                }
            } catch (e: Exception) {
                // Ignore non-fatal error
            }
        }
    }

    fun addSnapshot(create: com.riversongai.data.model.SnapshotCreate) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val resp = apiService.addSnapshot(create)
                if (resp.isSuccessful) {
                    loadData()
                } else {
                    _error.value = "Failed to add snapshot"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSnapshot(id: String) {
        viewModelScope.launch {
            try {
                val resp = apiService.deleteSnapshot(id)
                if (resp.isSuccessful) {
                    loadData()
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearError() { _error.value = null }
}
