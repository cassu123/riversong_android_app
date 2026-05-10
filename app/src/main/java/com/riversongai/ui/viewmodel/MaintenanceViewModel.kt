package com.riversongai.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riversongai.data.model.*
import com.riversongai.data.repository.MaintenanceRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MaintenanceViewModel(private val repo: MaintenanceRepository) : ViewModel() {

    private val _vehicles = MutableLiveData<List<Vehicle>>(emptyList())
    val vehicles: LiveData<List<Vehicle>> = _vehicles

    private val _selectedVehicle = MutableLiveData<Vehicle?>()
    val selectedVehicle: LiveData<Vehicle?> = _selectedVehicle

    private val _serviceLogs = MutableLiveData<List<ServiceLog>>(emptyList())
    val serviceLogs: LiveData<List<ServiceLog>> = _serviceLogs

    private val _assignments = MutableLiveData<List<VehicleAssignment>>(emptyList())
    val assignments: LiveData<List<VehicleAssignment>> = _assignments

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

    fun selectVehicle(v: Vehicle) {
        _selectedVehicle.value = v
        loadVehicleData(v.id)
    }

    fun loadVehicleData(vehicleId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val logsJob = async { repo.getServiceLogs(vehicleId).onSuccess { _serviceLogs.value = it } }
            val assignJob = async { repo.getAssignments(vehicleId).onSuccess { _assignments.value = it } }
            logsJob.await()
            assignJob.await()
            _isLoading.value = false
        }
    }

    fun createVehicle(body: CreateVehicle) {
        viewModelScope.launch {
            _isLoading.value = true
            repo.createVehicle(body)
                .onSuccess {
                    _vehicles.value = (_vehicles.value ?: emptyList()) + it
                    selectVehicle(it)
                    _toast.value = "Vehicle added"
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun deleteVehicle(id: String) {
        viewModelScope.launch {
            repo.deleteVehicle(id).onSuccess {
                _vehicles.value = _vehicles.value?.filter { it.id != id }
                if (_selectedVehicle.value?.id == id) _selectedVehicle.value = null
                _toast.value = "Vehicle removed"
            }
        }
    }

    fun logService(body: CreateServiceLog) {
        val vehicleId = _selectedVehicle.value?.id ?: return
        viewModelScope.launch {
            _isLoading.value = true
            repo.createServiceLog(vehicleId, body)
                .onSuccess {
                    _serviceLogs.value = listOf(it) + (_serviceLogs.value ?: emptyList())
                    _toast.value = "Service logged"
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun clearError() { _error.value = null }
    fun clearToast() { _toast.value = null }
}
