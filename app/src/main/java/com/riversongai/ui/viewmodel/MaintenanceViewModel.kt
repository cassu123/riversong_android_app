package com.riversongai.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riversongai.data.model.CreateServiceLog
import com.riversongai.data.model.CreateVehicle
import com.riversongai.data.model.ServiceCheckpoint
import com.riversongai.data.model.ServiceLog
import com.riversongai.data.model.Vehicle
import com.riversongai.data.repository.MaintenanceRepository
import kotlinx.coroutines.launch

class MaintenanceViewModel(private val repo: MaintenanceRepository) : ViewModel() {

    private val _vehicles = MutableLiveData<List<Vehicle>>(emptyList())
    val vehicles: LiveData<List<Vehicle>> = _vehicles

    private val _selectedVehicle = MutableLiveData<Vehicle?>()
    val selectedVehicle: LiveData<Vehicle?> = _selectedVehicle

    private val _checkpoints = MutableLiveData<List<ServiceCheckpoint>>(emptyList())
    val checkpoints: LiveData<List<ServiceCheckpoint>> = _checkpoints

    private val _serviceLogs = MutableLiveData<List<ServiceLog>>(emptyList())
    val serviceLogs: LiveData<List<ServiceLog>> = _serviceLogs

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _toast = MutableLiveData<String?>()
    val toast: LiveData<String?> = _toast

    fun loadVehicles() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.getVehicles()
                .onSuccess {
                    _vehicles.value = it
                    if (it.isNotEmpty() && _selectedVehicle.value == null) selectVehicle(it.first())
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun selectVehicle(vehicle: Vehicle) {
        _selectedVehicle.value = vehicle
        loadCheckpoints(vehicle.id)
        loadServiceLogs(vehicle.id)
    }

    private fun loadCheckpoints(vehicleId: String) {
        viewModelScope.launch {
            repo.getCheckpoints(vehicleId)
                .onSuccess { _checkpoints.value = it }
                .onFailure { /* silent — checkpoints may not exist yet */ }
        }
    }

    fun loadServiceLogs(vehicleId: String) {
        viewModelScope.launch {
            repo.getServiceLogs(vehicleId)
                .onSuccess { _serviceLogs.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    fun createVehicle(body: CreateVehicle) {
        viewModelScope.launch {
            _isLoading.value = true
            repo.createVehicle(body)
                .onSuccess { v ->
                    _vehicles.value = (_vehicles.value ?: emptyList()) + v
                    selectVehicle(v)
                    _toast.value = "${v.year} ${v.make} ${v.model} added"
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun deleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            repo.deleteVehicle(vehicleId)
                .onSuccess {
                    val remaining = _vehicles.value?.filter { it.id != vehicleId } ?: emptyList()
                    _vehicles.value = remaining
                    if (_selectedVehicle.value?.id == vehicleId) {
                        val next = remaining.firstOrNull()
                        _selectedVehicle.value = next
                        if (next != null) selectVehicle(next) else { _checkpoints.value = emptyList(); _serviceLogs.value = emptyList() }
                    }
                    _toast.value = "Vehicle removed"
                }
                .onFailure { _error.value = it.message }
        }
    }

    fun logService(body: CreateServiceLog) {
        val vehicleId = _selectedVehicle.value?.id ?: return
        viewModelScope.launch {
            _isLoading.value = true
            repo.createServiceLog(vehicleId, body)
                .onSuccess { log ->
                    _serviceLogs.value = listOf(log) + (_serviceLogs.value ?: emptyList())
                    _toast.value = "Service logged"
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun clearError() { _error.value = null }
    fun clearToast() { _toast.value = null }
}
