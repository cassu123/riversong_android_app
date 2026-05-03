package com.riversongai.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.riversongai.data.model.Routine
import com.riversongai.data.model.RoutineCreate
import com.riversongai.data.repository.RoutinesRepository
import com.riversongai.utils.NotificationHelper
import kotlinx.coroutines.launch

class RoutinesViewModel(app: Application, private val routinesRepository: RoutinesRepository) : AndroidViewModel(app) {

    private val _routines = MutableLiveData<List<Routine>>(emptyList())
    val routines: LiveData<List<Routine>> = _routines

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _actionResult = MutableLiveData<String?>()
    val actionResult: LiveData<String?> = _actionResult

    private val _routineRunOutput = MutableLiveData<String?>()
    val routineRunOutput: LiveData<String?> = _routineRunOutput

    private val _editingRoutine = MutableLiveData<Routine?>(null)
    val editingRoutine: LiveData<Routine?> = _editingRoutine

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

    fun createRoutine(name: String, prompt: String, trigger: String, time: String?, days: List<String>?) {
        if (name.isBlank()) {
            _error.value = "Routine name cannot be empty"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val create = RoutineCreate(
                name = name.trim(),
                prompt = prompt.trim(),
                triggerType = trigger,
                scheduleTime = time,
                scheduleDays = days
            )
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

    fun updateRoutine(routineId: String, name: String, prompt: String, trigger: String, time: String?, days: List<String>?, enabled: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            val fields = mapOf(
                "name" to name.trim(),
                "prompt" to prompt.trim(),
                "trigger_type" to trigger,
                "schedule_time" to time,
                "schedule_days" to days,
                "is_enabled" to enabled
            )
            routinesRepository.updateRoutine(routineId, fields).fold(
                onSuccess = {
                    _actionResult.value = "Routine updated"
                    loadRoutines()
                },
                onFailure = { _error.value = it.message }
            )
            _isLoading.value = false
        }
    }

    fun setEditingRoutine(routine: Routine?) {
        _editingRoutine.value = routine
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
        val routineName = _routines.value?.find { it.id == routineId }?.name ?: "Routine"
        viewModelScope.launch {
            _isLoading.value = true
            routinesRepository.runRoutine(routineId).fold(
                onSuccess = { response ->
                    _routineRunOutput.value = response.output
                    NotificationHelper.showRoutineComplete(getApplication(), routineName, response.output)
                    loadRoutines()
                },
                onFailure = { _error.value = it.message }
            )
            _isLoading.value = false
        }
    }

    fun clearActionResult() { _actionResult.value = null }
    fun clearError() { _error.value = null }
    fun clearRoutineRunOutput() { _routineRunOutput.value = null }
}
