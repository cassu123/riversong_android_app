package com.riversongai.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riversongai.data.model.*
import com.riversongai.data.repository.CommerceRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class CommerceViewModel(private val repo: CommerceRepository) : ViewModel() {

    private val _workspaces = MutableLiveData<List<CommerceWorkspace>>(emptyList())
    val workspaces: LiveData<List<CommerceWorkspace>> = _workspaces

    private val _selectedWorkspace = MutableLiveData<CommerceWorkspace?>()
    val selectedWorkspace: LiveData<CommerceWorkspace?> = _selectedWorkspace

    private val _products = MutableLiveData<List<Product>>(emptyList())
    val products: LiveData<List<Product>> = _products

    private val _suppliers = MutableLiveData<List<Supplier>>(emptyList())
    val suppliers: LiveData<List<Supplier>> = _suppliers

    private val _customers = MutableLiveData<List<Customer>>(emptyList())
    val customers: LiveData<List<Customer>> = _customers

    private val _sales = MutableLiveData<List<Sale>>(emptyList())
    val sales: LiveData<List<Sale>> = _sales

    private val _members = MutableLiveData<List<WorkspaceMember>>(emptyList())
    val members: LiveData<List<WorkspaceMember>> = _members

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _toast = MutableLiveData<String?>()
    val toast: LiveData<String?> = _toast

    fun loadWorkspaces() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.getWorkspaces()
                .onSuccess { list ->
                    _workspaces.value = list
                    if (list.isNotEmpty() && _selectedWorkspace.value == null) {
                        selectWorkspace(list.first())
                    }
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun selectWorkspace(ws: CommerceWorkspace) {
        _selectedWorkspace.value = ws
        loadWorkspaceData(ws.id)
    }

    fun loadWorkspaceData(workspaceId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val pJob = async { repo.getProducts(workspaceId) }
            val sJob = async { repo.getSuppliers(workspaceId) }
            val cJob = async { repo.getCustomers(workspaceId) }
            val slJob = async { repo.getSales(workspaceId) }
            val mJob = async { repo.getMembers(workspaceId) }

            pJob.await().onSuccess { _products.value = it }
            sJob.await().onSuccess { _suppliers.value = it }
            cJob.await().onSuccess { _customers.value = it }
            slJob.await().onSuccess { _sales.value = it }
            mJob.await().onSuccess { _members.value = it }
            
            _isLoading.value = false
        }
    }

    fun createWorkspace(name: String, description: String = "") {
        viewModelScope.launch {
            _isLoading.value = true
            repo.createWorkspace(name, description)
                .onSuccess { ws ->
                    _workspaces.value = _workspaces.value.orEmpty() + ws
                    selectWorkspace(ws)
                    _toast.value = "Store \"${ws.name}\" created"
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun createProduct(body: CreateProduct) {
        val wsId = _selectedWorkspace.value?.id ?: return
        viewModelScope.launch {
            _isLoading.value = true
            repo.createProduct(wsId, body)
                .onSuccess { p ->
                    _products.value = _products.value.orEmpty() + p
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
