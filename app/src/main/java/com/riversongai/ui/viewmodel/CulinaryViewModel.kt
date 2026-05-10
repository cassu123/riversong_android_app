package com.riversongai.ui.viewmodel

import androidx.lifecycle.*
import com.riversongai.data.model.*
import com.riversongai.data.remote.RiverSongApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class CulinaryViewModel(private val apiService: RiverSongApiService) : ViewModel() {

    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> = _recipes

    private val _bannedItems = MutableLiveData<List<BannedItem>>()
    val bannedItems: LiveData<List<BannedItem>> = _bannedItems

    private val _equipment = MutableLiveData<List<KitchenEquipment>>()
    val equipment: LiveData<List<KitchenEquipment>> = _equipment

    private val _dinnerProposals = MutableLiveData<List<DinnerProposal>>()
    val dinnerProposals: LiveData<List<DinnerProposal>> = _dinnerProposals

    private val _stockroom = MutableLiveData<List<StockroomItem>>()
    val stockroom: LiveData<List<StockroomItem>> = _stockroom

    private val _activePrep = MutableLiveData<PrepSession?>()
    val activePrep: LiveData<PrepSession?> = _activePrep

    private val _walmartMappings = MutableLiveData<List<WalmartMapping>>()
    val walmartMappings: LiveData<List<WalmartMapping>> = _walmartMappings

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _searchQuery = MutableLiveData("")
    private val _selectedMealType = MutableLiveData("All")

    val filteredRecipes: LiveData<List<Recipe>> = _searchQuery.switchMap { query ->
        _selectedMealType.map { mealType ->
            _recipes.value.orEmpty().filter {
                (mealType == "All" || it.mealType.equals(mealType, ignoreCase = true)) &&
                (query.isBlank() || it.title.contains(query, ignoreCase = true))
            }
        }
    }

    fun loadAll() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val rJob = async { loadRecipes() }
                val bJob = async { loadBannedItems() }
                val eJob = async { loadEquipment() }
                val dJob = async { loadDinnerProposals() }
                val sJob = async { loadStockroom() }
                val pJob = async { loadActivePrep() }
                val wJob = async { loadWalmartMappings() }
                rJob.await(); bJob.await(); eJob.await(); dJob.await(); sJob.await(); pJob.await(); wJob.await()
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun loadRecipes() {
        try {
            val resp = apiService.getRecipes()
            if (resp.isSuccessful) _recipes.postValue(resp.body() ?: emptyList())
        } catch (e: Exception) { _error.postValue(e.message) }
    }

    suspend fun loadBannedItems() {
        try {
            val resp = apiService.getBannedItems()
            if (resp.isSuccessful) _bannedItems.postValue(resp.body() ?: emptyList())
        } catch (e: Exception) { }
    }

    suspend fun loadEquipment() {
        try {
            val resp = apiService.getEquipment()
            if (resp.isSuccessful) _equipment.postValue(resp.body() ?: emptyList())
        } catch (e: Exception) { }
    }

    suspend fun loadDinnerProposals() {
        try {
            val resp = apiService.getDinnerProposals()
            if (resp.isSuccessful) _dinnerProposals.postValue(resp.body() ?: emptyList())
        } catch (e: Exception) { }
    }

    suspend fun loadStockroom() {
        try {
            val resp = apiService.getStockroom()
            if (resp.isSuccessful) _stockroom.postValue(resp.body() ?: emptyList())
        } catch (e: Exception) { }
    }

    suspend fun loadActivePrep() {
        try {
            val resp = apiService.getActivePrep()
            if (resp.isSuccessful) _activePrep.postValue(resp.body())
            else if (resp.code() == 404) _activePrep.postValue(null)
        } catch (e: Exception) { }
    }

    suspend fun loadWalmartMappings() {
        try {
            val resp = apiService.getWalmartMappings()
            if (resp.isSuccessful) _walmartMappings.postValue(resp.body() ?: emptyList())
        } catch (e: Exception) { }
    }

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setMealType(mealType: String) { _selectedMealType.value = mealType }

    // --- Actions ---

    fun ingestRecipe(url: String?, file: File?, force: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val urlPart = url?.toRequestBody("text/plain".toMediaTypeOrNull())
                val filePart = file?.let {
                    val requestFile = it.asRequestBody("application/pdf".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("file", it.name, requestFile)
                }
                val resp = apiService.ingestRecipe(urlPart, filePart, force)
                if (resp.isSuccessful) loadRecipes()
                else _error.value = resp.errorBody()?.string() ?: "Ingest failed"
            } catch (e: Exception) { _error.value = e.message }
            finally { _isLoading.value = false }
        }
    }

    fun deleteRecipe(id: String) {
        viewModelScope.launch {
            try {
                if (apiService.deleteRecipe(id).isSuccessful) loadRecipes()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun addBannedItem(name: String, sub: String?) {
        viewModelScope.launch {
            try {
                if (apiService.addBannedItem(BannedItemCreate(name, sub)).isSuccessful) loadBannedItems()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun deleteBannedItem(id: String) {
        viewModelScope.launch {
            try {
                if (apiService.deleteBannedItem(id).isSuccessful) loadBannedItems()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun addEquipment(make: String, model: String) {
        viewModelScope.launch {
            try {
                if (apiService.addEquipment(EquipmentCreate(make, model)).isSuccessful) loadEquipment()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun deleteEquipment(id: String) {
        viewModelScope.launch {
            try {
                if (apiService.deleteEquipment(id).isSuccessful) loadEquipment()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun voteDinner(id: String, vote: String) {
        viewModelScope.launch {
            try {
                if (apiService.voteDinner(id, DinnerVoteRequest(vote)).isSuccessful) loadDinnerProposals()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun cookNow(id: String, onResult: (CookNowResponse) -> Unit) {
        viewModelScope.launch {
            try {
                val resp = apiService.cookNowDinner(id)
                if (resp.isSuccessful) {
                    resp.body()?.let { onResult(it) }
                    loadDinnerProposals()
                }
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun sendToPrep(recipeId: String, proposalId: String?) {
        viewModelScope.launch {
            try {
                var session = _activePrep.value
                if (session == null) {
                    val resp = apiService.createPrepSession(PrepSessionCreate("Tonight's Dinner"))
                    if (resp.isSuccessful) session = resp.body()
                }
                session?.let {
                    if (apiService.addRecipeToPrep(it.id, AddRecipeToPrep(recipeId)).isSuccessful) {
                        loadActivePrep()
                        proposalId?.let { pid -> apiService.dismissDinner(pid) }
                        loadDinnerProposals()
                    }
                }
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun scanStockroom(barcode: String) {
        viewModelScope.launch {
            try {
                if (apiService.scanStockroomBarcode(ScanRequest(barcode)).isSuccessful) loadStockroom()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun depleteStockroom(barcode: String) {
        viewModelScope.launch {
            try {
                if (apiService.depleteStockroomItem(ScanRequest(barcode)).isSuccessful) loadStockroom()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun createWalmartMapping(name: String, itemId: String) {
        viewModelScope.launch {
            try {
                if (apiService.createWalmartMapping(WalmartMappingCreate(name, itemId)).isSuccessful) loadWalmartMappings()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun deleteWalmartMapping(id: String) {
        viewModelScope.launch {
            try {
                if (apiService.deleteWalmartMapping(id).isSuccessful) loadWalmartMappings()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun exportWalmart(onResult: (WalmartExportResponse) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val resp = apiService.exportWalmartCart(_activePrep.value?.id)
                if (resp.isSuccessful) resp.body()?.let { onResult(it) }
            } catch (e: Exception) { _error.value = e.message }
            finally { _isLoading.value = false }
        }
    }

    fun clearError() { _error.value = null }
}
