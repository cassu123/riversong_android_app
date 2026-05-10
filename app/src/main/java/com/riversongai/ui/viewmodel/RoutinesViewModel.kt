package com.riversongai.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riversongai.data.model.Routine
import com.riversongai.data.model.RoutineCreate
import com.riversongai.data.repository.RoutinesRepository
import kotlinx.coroutines.launch

class RoutinesViewModel(private val repository: RoutinesRepository) : ViewModel() {

    private val _routines = MutableLiveData<List<Routine>>(emptyList())
    val routines: LiveData<List<Routine>> = _routines

    private val _n8nStatus = MutableLiveData<Map<String, Any?>>()
    val n8nStatus: LiveData<Map<String, Any?>> = _n8nStatus

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _routineOutput = MutableLiveData<Pair<String, String>?>()
    val routineOutput: LiveData<Pair<String, String>?> = _routineOutput

    init {
        loadRoutines()
        loadN8nStatus()
    }

    fun loadRoutines() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getRoutines()
                .onSuccess { _routines.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun loadN8nStatus() {
        viewModelScope.launch {
            repository.getN8nStatus().onSuccess { _n8nStatus.value = it }
        }
    }

    fun createRoutine(body: RoutineCreate) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.createRoutine(body)
                .onSuccess { loadRoutines() }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun updateRoutine(id: String, fields: Map<String, Any?>) {
        viewModelScope.launch {
            repository.updateRoutine(id, fields)
                .onSuccess { loadRoutines() }
                .onFailure { _error.value = it.message }
        }
    }

    fun deleteRoutine(id: String) {
        viewModelScope.launch {
            repository.deleteRoutine(id)
                .onSuccess { loadRoutines() }
                .onFailure { _error.value = it.message }
        }
    }

    fun runRoutine(routine: Routine) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.runRoutine(routine.id)
                .onSuccess { _routineOutput.value = routine.name to it.output }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun toggleRoutine(routine: Routine) {
        updateRoutine(routine.id, mapOf("enabled" to !routine.enabled))
    }

    fun clearOutput() { _routineOutput.value = null }
    fun clearError() { _error.value = null }
}
