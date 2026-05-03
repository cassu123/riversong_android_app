package com.riversongai.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riversongai.data.model.Routine
import com.riversongai.data.model.RoutineCreate
import com.riversongai.data.repository.RoutinesRepository
import kotlinx.coroutines.launch

class RoutinesViewModel(private val routinesRepository: RoutinesRepository) : ViewModel() {

    private val _routines = MutableLiveData<List<Routine>>(emptyList())
    val routines: LiveData<List<Routine>> = _routines

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _actionResult = MutableLiveData<String?>()
    val actionResult: LiveData<String?> = _actionResult

    init {
        loadRoutines()
    }

    fun loadRoutines() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            routinesRepository.getRoutines().fold(
                onSuccess = { _routines.value = it },
                onFailure = { _error.value = it.message }
            )
            _isLoading.value = false
        }
    }

    fun createRoutine(name: String, prompt: String) {
        if (name.isBlank()) {
            _error.value = "Routine name cannot be empty"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val create = RoutineCreate(name = name.trim(), prompt = prompt.trim())
            routinesRepository.createRoutine(create).fold(
                onSuccess = {
                    _actionResult.value = "Routine created"
                    loadRoutines()
                },
                onFailure = { _error.value = it.message }
            )
            _isLoading.value = false
        }
    }

    fun toggleRoutine(routineId: String, enabled: Boolean) {
        viewModelScope.launch {
            routinesRepository.toggleRoutine(routineId, enabled).fold(
                onSuccess = { updated ->
                    val current = _routines.value.orEmpty().toMutableList()
                    val idx = current.indexOfFirst { it.id == routineId }
                    if (idx >= 0) {
                        current[idx] = updated
                        _routines.value = current
                    }
                },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun deleteRoutine(routineId: String) {
        viewModelScope.launch {
            routinesRepository.deleteRoutine(routineId).fold(
                onSuccess = {
                    _actionResult.value = "Routine deleted"
                    _routines.value = _routines.value.orEmpty().filter { it.id != routineId }
                },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun runRoutine(routineId: String) {
        viewModelScope.launch {
            routinesRepository.runRoutine(routineId).fold(
                onSuccess = { output ->
                    _actionResult.value = if (output.isNotBlank()) output else "Routine ran successfully"
                    loadRoutines()
                },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun clearActionResult() { _actionResult.value = null }
    fun clearError() { _error.value = null }
}
