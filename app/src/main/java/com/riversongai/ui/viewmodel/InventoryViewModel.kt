package com.riversongai.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riversongai.data.model.CreateInventoryItem
import com.riversongai.data.model.InventoryHome
import com.riversongai.data.model.InventoryItem
import com.riversongai.data.repository.InventoryRepository
import kotlinx.coroutines.launch

class InventoryViewModel(private val repo: InventoryRepository) : ViewModel() {

    private val _homes = MutableLiveData<List<InventoryHome>>(emptyList())
    val homes: LiveData<List<InventoryHome>> = _homes

    private val _items = MutableLiveData<List<InventoryItem>>(emptyList())
    val items: LiveData<List<InventoryItem>> = _items

    private val _selectedHome = MutableLiveData<InventoryHome?>()
    val selectedHome: LiveData<InventoryHome?> = _selectedHome

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _toast = MutableLiveData<String?>()
    val toast: LiveData<String?> = _toast

    fun loadHomes() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.getHomes()
                .onSuccess { _homes.value = it; if (it.isNotEmpty() && _selectedHome.value == null) selectHome(it.first()) }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun selectHome(home: InventoryHome) {
        _selectedHome.value = home
        loadItems(home.id)
    }

    fun loadItems(homeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repo.getItems(homeId)
                .onSuccess { _items.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun createHome(name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repo.createHome(name)
                .onSuccess { newHome ->
                    _homes.value = (_homes.value ?: emptyList()) + newHome
                    selectHome(newHome)
                    _toast.value = "Home \"${newHome.name}\" created"
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun createItem(body: CreateInventoryItem) {
        val homeId = _selectedHome.value?.id ?: return
        viewModelScope.launch {
            _isLoading.value = true
            repo.createItem(homeId, body)
                .onSuccess { newItem ->
                    _items.value = (_items.value ?: emptyList()) + newItem
                    _toast.value = "\"${newItem.name}\" added"
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun updateItem(itemId: String, body: CreateInventoryItem) {
        viewModelScope.launch {
            _isLoading.value = true
            repo.updateItem(itemId, body)
                .onSuccess { updated ->
                    _items.value = _items.value?.map { if (it.id == itemId) updated else it }
                    _toast.value = "Updated"
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            repo.deleteItem(itemId)
                .onSuccess {
                    _items.value = _items.value?.filter { it.id != itemId }
                    _toast.value = "Item removed"
                }
                .onFailure { _error.value = it.message }
        }
    }

    fun clearError() { _error.value = null }
    fun clearToast() { _toast.value = null }
}
