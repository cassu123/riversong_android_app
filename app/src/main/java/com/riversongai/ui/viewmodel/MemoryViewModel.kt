package com.riversongai.ui.viewmodel

import androidx.lifecycle.*
import com.riversongai.data.model.*
import com.riversongai.data.repository.MemoryRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MemoryViewModel(private val memoryRepository: MemoryRepository) : ViewModel() {

    private val _facts = MutableLiveData<List<Fact>>(emptyList())
    val facts: LiveData<List<Fact>> = _facts
    
    private val _preferences = MutableLiveData<List<MemoryPreference>>(emptyList())
    val preferences: LiveData<List<MemoryPreference>> = _preferences

    private val _summaries = MutableLiveData<List<MemorySummary>>(emptyList())
    val summaries: LiveData<List<MemorySummary>> = _summaries

    private val _memoryStats = MutableLiveData<MemoryStats>()
    val memoryStats: LiveData<MemoryStats> = _memoryStats

    private val _filterQuery = MutableLiveData("")
    val filterQuery: LiveData<String> = _filterQuery

    val filteredFacts: LiveData<List<Fact>> = _filterQuery.switchMap { query ->
        _facts.map { list ->
            if (query.isBlank()) list
            else list.filter { it.key.contains(query, ignoreCase = true) || it.value.contains(query, ignoreCase = true) }
        }
    }

    val filteredPreferences: LiveData<List<MemoryPreference>> = _filterQuery.switchMap { query ->
        _preferences.map { list ->
            if (query.isBlank()) list
            else list.filter { it.category.contains(query, ignoreCase = true) || it.value.contains(query, ignoreCase = true) }
        }
    }

    val filteredSummaries: LiveData<List<MemorySummary>> = _filterQuery.switchMap { query ->
        _summaries.map { list ->
            if (query.isBlank()) list
            else list.filter { it.summary.contains(query, ignoreCase = true) }
        }
    }

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _actionResult = MutableLiveData<String?>()
    val actionResult: LiveData<String?> = _actionResult

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val factsJob = async { memoryRepository.getFacts() }
            val prefsJob  = async { memoryRepository.getPreferences() }
            val sumsJob   = async { memoryRepository.getSummaries() }

            val f = factsJob.await()
            val p = prefsJob.await()
            val s = sumsJob.await()

            f.onSuccess { _facts.value = it }
            p.onSuccess { _preferences.value = it }
            s.onSuccess { _summaries.value = it }

            updateStats()
            _isLoading.value = false
        }
    }

    fun loadStats() = loadAll()

    private fun updateStats() {
        _memoryStats.value = MemoryStats(
            factsCount = _facts.value?.size ?: 0,
            prefsCount = _preferences.value?.size ?: 0,
            sessionsCount = _summaries.value?.size ?: 0
        )
    }

    fun setFilterQuery(query: String) {
        _filterQuery.value = query
    }

    fun addFact(key: String, value: String) {
        if (key.isBlank() || value.isBlank()) {
            _error.value = "Key and value cannot be empty"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            memoryRepository.createFact(key.trim(), value.trim()).fold(
                onSuccess = {
                    _actionResult.value = "Fact saved"
                    loadAll()
                },
                onFailure = { _error.value = it.message }
            )
            _isLoading.value = false
        }
    }

    fun deleteFact(factId: String) {
        viewModelScope.launch {
            memoryRepository.deleteFact(factId).fold(
                onSuccess = {
                    _actionResult.value = "Fact deleted"
                    _facts.value = _facts.value.orEmpty().filter { it.id != factId }
                    updateStats()
                },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun deleteFacts(ids: Set<String>) {
        viewModelScope.launch {
            _isLoading.value = true
            var successCount = 0
            ids.forEach { id ->
                memoryRepository.deleteFact(id).onSuccess { successCount++ }
            }
            if (successCount > 0) {
                _actionResult.value = "Removed $successCount facts"
                loadAll()
            }
            _isLoading.value = false
        }
    }

    fun deletePreference(id: String) {
        viewModelScope.launch {
            memoryRepository.deletePreference(id).fold(
                onSuccess = {
                    _actionResult.value = "Preference deleted"
                    _preferences.value = _preferences.value.orEmpty().filter { it.id != id }
                    updateStats()
                },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun deleteSummary(id: String) {
        viewModelScope.launch {
            memoryRepository.deleteSummary(id).fold(
                onSuccess = {
                    _actionResult.value = "Summary deleted"
                    _summaries.value = _summaries.value.orEmpty().filter { it.id != id }
                    updateStats()
                },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun clearActionResult() { _actionResult.value = null }
    fun clearError() { _error.value = null }
}
