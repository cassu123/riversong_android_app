package com.riversongai.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riversongai.data.model.CommerceWorkspace
import com.riversongai.data.model.CreateProduct
import com.riversongai.data.model.Product
import com.riversongai.data.repository.CommerceRepository
import kotlinx.coroutines.launch

class CommerceViewModel(private val repo: CommerceRepository) : ViewModel() {

    private val _workspace = MutableLiveData<CommerceWorkspace?>()
    val workspace: LiveData<CommerceWorkspace?> = _workspace

    private val _products = MutableLiveData<List<Product>>(emptyList())
    val products: LiveData<List<Product>> = _products

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _toast = MutableLiveData<String?>()
    val toast: LiveData<String?> = _toast

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.getWorkspaces()
                .onSuccess { workspaces ->
                    val ws = workspaces.firstOrNull()
                    _workspace.value = ws
                    if (ws != null) loadProducts(ws.id)
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    private fun loadProducts(workspaceId: String) {
        viewModelScope.launch {
            repo.getProducts(workspaceId)
                .onSuccess { _products.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    fun createWorkspace(name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repo.createWorkspace(name)
                .onSuccess { ws ->
                    _workspace.value = ws
                    _products.value = emptyList()
                    _toast.value = "Store \"${ws.name}\" created"
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun createProduct(body: CreateProduct) {
        val wsId = _workspace.value?.id ?: return
        viewModelScope.launch {
            _isLoading.value = true
            repo.createProduct(wsId, body)
                .onSuccess { p ->
                    _products.value = (_products.value ?: emptyList()) + p
                    _toast.value = "\"${p.name}\" added"
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun updateProduct(productId: String, body: CreateProduct) {
        viewModelScope.launch {
            _isLoading.value = true
            repo.updateProduct(productId, body)
                .onSuccess { updated ->
                    _products.value = _products.value?.map { if (it.id == productId) updated else it }
                    _toast.value = "Updated"
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repo.deleteProduct(productId)
                .onSuccess {
                    _products.value = _products.value?.filter { it.id != productId }
                    _toast.value = "Product removed"
                }
                .onFailure { _error.value = it.message }
        }
    }

    fun adjustStock(productId: String, delta: Int) {
        viewModelScope.launch {
            repo.adjustStock(productId, delta)
                .onSuccess { updated ->
                    _products.value = _products.value?.map { if (it.id == productId) updated else it }
                }
                .onFailure { _error.value = it.message }
        }
    }

    fun clearError() { _error.value = null }
    fun clearToast() { _toast.value = null }
}
