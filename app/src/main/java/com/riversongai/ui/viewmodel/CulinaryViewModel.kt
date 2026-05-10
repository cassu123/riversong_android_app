package com.riversongai.ui.viewmodel

import androidx.lifecycle.*
import com.riversongai.data.model.*
import com.riversongai.data.remote.RiverSongApiService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CulinaryViewModel(private val apiService: RiverSongApiService) : ViewModel() {

    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> = _recipes

    private val _bannedItems = MutableLiveData<List<BannedItem>>()
    val bannedItems: LiveData<List<BannedItem>> = _bannedItems

    private val _household = MutableLiveData<CulinaryHousehold?>()
    val household: LiveData<CulinaryHousehold?> = _household

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _searchQuery = MutableLiveData("")
    private val _selectedMealType = MutableLiveData("All")

    val filteredRecipes: LiveData<List<Recipe>> = Transformations.switchMap(_searchQuery) { query ->
        Transformations.map(_selectedMealType) { mealType ->
            _recipes.value.orEmpty().filter {
                (mealType == "All" || it.mealType.equals(mealType, ignoreCase = true)) &&
                (query.isBlank() || it.title.contains(query, ignoreCase = true))
            }
        }
    }

    fun loadRecipes() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val resp = apiService.getRecipes()
                if (resp.isSuccessful) _recipes.value = resp.body() ?: emptyList()
            } catch (e: Exception) { _error.value = e.message }
            finally { _isLoading.value = false }
        }
    }

    fun loadBannedItems() {
        viewModelScope.launch {
            try {
                val resp = apiService.getBannedItems()
                if (resp.isSuccessful) _bannedItems.value = resp.body() ?: emptyList()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun loadHousehold() {
        viewModelScope.launch {
            try {
                val resp = apiService.getCulinaryHousehold()
                if (resp.isSuccessful) _household.value = resp.body()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setMealType(mealType: String) { _selectedMealType.value = mealType }

    fun addRecipe(create: RecipeCreate) {
        viewModelScope.launch {
            try {
                val resp = apiService.createRecipe(create)
                if (resp.isSuccessful) loadRecipes()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun addBannedItem(create: BannedItemCreate) {
        viewModelScope.launch {
            try {
                val resp = apiService.addBannedItem(create)
                if (resp.isSuccessful) loadBannedItems()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun deleteBannedItem(id: String) {
        viewModelScope.launch {
            try {
                val resp = apiService.deleteBannedItem(id)
                if (resp.isSuccessful) loadBannedItems()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    private var equipmentUpdateJob: Job? = null
    fun updateEquipment(equipment: Map<String, Boolean>) {
        // Debounce update
        equipmentUpdateJob?.cancel()
        equipmentUpdateJob = viewModelScope.launch {
            delay(1000)
            try {
                val resp = apiService.updateEquipment(EquipmentUpdate(equipment))
                if (resp.isSuccessful) _household.value = resp.body()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun clearError() { _error.value = null }
}
