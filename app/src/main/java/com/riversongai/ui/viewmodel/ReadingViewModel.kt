package com.riversongai.ui.viewmodel

import androidx.lifecycle.*
import com.riversongai.data.model.*
import com.riversongai.data.remote.RiverSongApiService
import kotlinx.coroutines.launch

class ReadingViewModel(private val apiService: RiverSongApiService) : ViewModel() {

    private val _books = MutableLiveData<List<Book>>()
    val books: LiveData<List<Book>> = _books

    private val _stats = MutableLiveData<ReadingStats?>()
    val stats: LiveData<ReadingStats?> = _stats

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _statusFilter = MutableLiveData("All")
    val statusFilter: LiveData<String> = _statusFilter

    val filteredBooks: LiveData<List<Book>> = Transformations.switchMap(_statusFilter) { filter ->
        Transformations.map(_books) { list ->
            if (filter == "All") list
            else list.filter { it.status.equals(filter.replace(" ", "_"), ignoreCase = true) }
        }
    }

    fun loadData() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val shelfResp = apiService.getReadingShelf()
                if (shelfResp.isSuccessful) {
                    _books.value = shelfResp.body() ?: emptyList()
                }

                val statsResp = apiService.getReadingStats()
                if (statsResp.isSuccessful) {
                    _stats.value = statsResp.body()
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setStatusFilter(filter: String) {
        _statusFilter.value = filter
    }

    fun addBook(create: BookCreate) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val resp = apiService.addBook(create)
                if (resp.isSuccessful) {
                    loadData()
                } else {
                    _error.value = "Failed to add book: ${resp.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateBook(bookId: String, update: BookUpdate) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val resp = apiService.updateBook(bookId, update)
                if (resp.isSuccessful) {
                    loadData()
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteBook(bookId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val resp = apiService.deleteBook(bookId)
                if (resp.isSuccessful) {
                    loadData()
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() { _error.value = null }
}
