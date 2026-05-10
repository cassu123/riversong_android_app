package com.riversongai.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riversongai.data.model.*
import com.riversongai.data.repository.InventoryRepository
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class InventoryViewModel(private val repo: InventoryRepository) : ViewModel() {

    private val _homes = MutableLiveData<List<InventoryHome>>(emptyList())
    val homes: LiveData<List<InventoryHome>> = _homes

    private val _items = MutableLiveData<List<InventoryItem>>(emptyList())
    val items: LiveData<List<InventoryItem>> = _items

    private val _selectedHome = MutableLiveData<InventoryHome?>()
    val selectedHome: LiveData<InventoryHome?> = _selectedHome

    private val _activeAudit = MutableLiveData<InventoryAudit?>()
    val activeAudit: LiveData<InventoryAudit?> = _activeAudit

    private val _attachments = MutableLiveData<Map<String, List<ItemAttachment>>>(emptyMap())
    val attachments: LiveData<Map<String, List<ItemAttachment>>> = _attachments

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
        loadActiveAudit(home.id)
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

    fun loadActiveAudit(homeId: String) {
        viewModelScope.launch {
            repo.getActiveAudit(homeId).onSuccess { _activeAudit.value = it }
        }
    }

    fun startAudit() {
        val homeId = _selectedHome.value?.id ?: return
        viewModelScope.launch {
            _isLoading.value = true
            repo.startAudit(homeId).onSuccess { _activeAudit.value = it }
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

    fun updateItem(itemId: String, fields: Map<String, Any?>) {
        viewModelScope.launch {
            _isLoading.value = true
            repo.updateItem(itemId, fields)
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

    fun loadAttachments(itemId: String) {
        viewModelScope.launch {
            repo.getAttachments(itemId).onSuccess { list ->
                val current = _attachments.value.orEmpty().toMutableMap()
                current[itemId] = list
                _attachments.value = current
            }
        }
    }

    fun uploadAttachment(itemId: String, file: File) {
        viewModelScope.launch {
            val requestFile = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            repo.uploadAttachment(itemId, body).onSuccess { loadAttachments(itemId) }
        }
    }

    fun issueItem(itemId: String, email: String) {
        viewModelScope.launch {
            repo.issueItem(itemId, email).onSuccess { updated ->
                _items.value = _items.value?.map { if (it.id == itemId) updated else it }
                _toast.value = "Issued to $email"
            }
        }
    }

    fun returnItem(itemId: String) {
        viewModelScope.launch {
            repo.returnItem(itemId).onSuccess { updated ->
                _items.value = _items.value?.map { if (it.id == itemId) updated else it }
                _toast.value = "Returned"
            }
        }
    }

    fun analyzePhoto(file: File, onResult: (String, String, String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", file.name, requestFile)
            repo.analyzePhoto(part).onSuccess { data ->
                onResult(data["name"] ?: "", data["category"] ?: "Other", data["description"] ?: "")
            }
            _isLoading.value = false
        }
    }

    fun clearError() { _error.value = null }
    fun clearToast() { _toast.value = null }
}
