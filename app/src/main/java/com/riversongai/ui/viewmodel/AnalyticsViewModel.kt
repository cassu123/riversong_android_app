package com.riversongai.ui.viewmodel

import androidx.lifecycle.*
import com.riversongai.data.model.*
import com.riversongai.data.remote.RiverSongApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class AnalyticsViewModel(private val apiService: RiverSongApiService) : ViewModel() {

    private val _snapshots = MutableLiveData<List<AnalyticsSnapshot>>(emptyList())
    val snapshots: LiveData<List<AnalyticsSnapshot>> = _snapshots

    private val _platforms = MutableLiveData<List<Map<String, Any?>>>(emptyList())
    val platforms: LiveData<List<Map<String, Any?>>> = _platforms

    private val _businessReport = MutableLiveData<String?>()
    val businessReport: LiveData<String?> = _businessReport

    private val _platformInsights = MutableLiveData<Map<String, String>>(emptyMap())
    val platformInsights: LiveData<Map<String, String>> = _platformInsights

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isGeneratingReport = MutableLiveData(false)
    val isGeneratingReport: LiveData<Boolean> = _isGeneratingReport

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _selectedRange = MutableLiveData(30)
    val selectedRange: LiveData<Int> = _selectedRange

    fun loadData(days: Int = _selectedRange.value ?: 30) {
        _selectedRange.value = days
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapsJob = async { apiService.getAnalyticsSnapshots(null, days) }
                val platsJob = async { apiService.getAnalyticsPlatforms() }

                val snapsResp = snapsJob.await()
                if (snapsResp.isSuccessful) _snapshots.value = snapsResp.body()

                val platsResp = platsJob.await()
                if (platsResp.isSuccessful) _platforms.value = platsResp.body()

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateBusinessReport(days: Int = _selectedRange.value ?: 30) {
        viewModelScope.launch {
            _isGeneratingReport.value = true
            try {
                val resp = apiService.getBusinessReport(days)
                if (resp.isSuccessful) {
                    _businessReport.value = resp.body()?.report
                } else {
                    _error.value = "Failed to generate report: ${resp.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isGeneratingReport.value = false
            }
        }
    }

    fun fetchPlatformSummary(platform: String) {
        viewModelScope.launch {
            try {
                val resp = apiService.getPlatformSummary(platform)
                if (resp.isSuccessful) {
                    val current = _platformInsights.value.orEmpty().toMutableMap()
                    resp.body()?.insights?.let { current[platform] = it }
                    _platformInsights.value = current
                }
            } catch (e: Exception) {}
        }
    }

    fun addSnapshot(snapshot: AnalyticsSnapshot) {
        viewModelScope.launch {
            try {
                val resp = apiService.addAnalyticsSnapshot(snapshot)
                if (resp.isSuccessful) loadData()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun deleteSnapshot(snapId: String) {
        viewModelScope.launch {
            try {
                val resp = apiService.deleteSnapshot(snapId)
                if (resp.isSuccessful) loadData()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun clearError() { _error.value = null }
}
