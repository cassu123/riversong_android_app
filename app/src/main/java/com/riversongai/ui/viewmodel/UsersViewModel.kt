package com.riversongai.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riversongai.data.model.AppUser
import com.riversongai.data.model.RoleUpdateBody
import com.riversongai.data.remote.RiverSongApiService
import kotlinx.coroutines.launch

class UsersViewModel(private val apiService: RiverSongApiService) : ViewModel() {

    private val _users = MutableLiveData<List<AppUser>>()
    val users: LiveData<List<AppUser>> = _users

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadUsers() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val resp = apiService.getUsers()
                if (resp.isSuccessful) {
                    _users.value = resp.body() ?: emptyList()
                } else {
                    _error.value = "Failed to load users: ${resp.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun approveUser(userId: String) {
        viewModelScope.launch {
            try {
                val resp = apiService.approveUser(userId)
                if (resp.isSuccessful) {
                    loadUsers()
                } else {
                    _error.value = "Failed to approve user"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun updateUserRole(userId: String, newRole: String) {
        viewModelScope.launch {
            try {
                val resp = apiService.updateUserRole(userId, RoleUpdateBody(newRole))
                if (resp.isSuccessful) {
                    val current = _users.value.orEmpty().toMutableList()
                    val index = current.indexOfFirst { it.id == userId }
                    if (index != -1 && resp.body() != null) {
                        current[index] = resp.body()!!
                        _users.value = current
                    }
                } else {
                    _error.value = "Failed to update role"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearError() { _error.value = null }
}
