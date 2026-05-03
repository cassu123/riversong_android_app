package com.riversongai.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riversongai.data.model.Fact
import com.riversongai.data.repository.MemoryRepository
import kotlinx.coroutines.launch

class MemoryViewModel(private val memoryRepository: MemoryRepository) : ViewModel() {

    private val _facts = MutableLiveData<List<Fact>>(emptyList())
    
    private val _filterQuery = MutableLiveData("")
    val filterQuery: LiveData<String> = _filterQuery

    val filteredFacts = MediatorLiveData<List<Fact>>().apply {
        addSource(_facts) { facts ->
            value = filterList(facts, _filterQuery.value.orEmpty())
        }
        addSource(_filterQuery) { query ->
            value = filterList(_facts.value.orEmpty(), query)
        }
    }

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _actionResult = MutableLiveData<String?>()
    val actionResult: LiveData<String?> = _actionResult

    init {
        loadFacts()
    }

    fun loadFacts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            memoryRepository.getFacts().fold(
                onSuccess = { _facts.value = it },
                onFailure = { _error.value = it.message }
            )
            _isLoading.value = false
        }
    }

    fun setFilterQuery(query: String) {
        _filterQuery.value = query
    }

    private fun filterList(facts: List<Fact>, query: String): List<Fact> {
        if (query.isBlank()) return facts
        return facts.filter {
            it.key.contains(query, ignoreCase = true) ||
            it.value.contains(query, ignoreCase = true)
        }
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
                    loadFacts()
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
                    val current = _facts.value.orEmpty().filter { it.id != factId }
                    _facts.value = current
                },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun clearActionResult() {
        _actionResult.value = null
    }

    fun clearError() {
        _error.value = null
    }
}
