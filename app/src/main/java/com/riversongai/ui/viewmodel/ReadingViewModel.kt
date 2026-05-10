package com.riversongai.ui.viewmodel

import androidx.lifecycle.*
import com.riversongai.data.model.*
import com.riversongai.data.remote.RiverSongApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ReadingViewModel(private val apiService: RiverSongApiService) : ViewModel() {

    private val _books = MutableLiveData<List<Book>>(emptyList())
    val books: LiveData<List<Book>> = _books

    private val _stats = MutableLiveData<ReadingStats?>()
    val stats: LiveData<ReadingStats?> = _stats

    private val _connections = MutableLiveData<ReadingConnections?>()
    val connections: LiveData<ReadingConnections?> = _connections

    private val _libbyLoans = MutableLiveData<List<LibbyLoan>>(emptyList())
    val libbyLoans: LiveData<List<LibbyLoan>> = _libbyLoans

    private val _libbyHolds = MutableLiveData<List<LibbyHold>>(emptyList())
    val libbyHolds: LiveData<List<LibbyHold>> = _libbyHolds

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _statusFilter = MutableLiveData("All")
    val statusFilter: LiveData<String> = _statusFilter

    private val _serviceFilter = MutableLiveData("All")
    val serviceFilter: LiveData<String> = _serviceFilter

    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> = _searchQuery

    val filteredBooks: LiveData<List<Book>> = _searchQuery.switchMap { query ->
        _statusFilter.switchMap { status ->
            _serviceFilter.map { service ->
                _books.value.orEmpty().filter { book ->
                    (query.isBlank() || book.title.contains(query, ignoreCase = true) || book.author.contains(query, ignoreCase = true)) &&
                    (status == "All" || book.status.equals(status.replace(" ", "_"), ignoreCase = true)) &&
                    (service == "All" || book.service.equals(service, ignoreCase = true))
                }
            }
        }
    }

    fun loadData() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val shelfJob = async { apiService.getReadingShelf() }
                val statsJob = async { apiService.getReadingStats() }
                val connJob  = async { apiService.getReadingConnections() }

                val shelfResp = shelfJob.await()
                if (shelfResp.isSuccessful) _books.value = shelfResp.body() ?: emptyList()

                val statsResp = statsJob.await()
                if (statsResp.isSuccessful) _stats.value = statsResp.body()

                val connResp = connJob.await()
                if (connResp.isSuccessful) _connections.value = connResp.body()
                
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadLibbyData() {
        viewModelScope.launch {
            try {
                val loansResp = apiService.getLibbyLoans()
                if (loansResp.isSuccessful) _libbyLoans.value = loansResp.body() ?: emptyList()

                val holdsResp = apiService.getLibbyHolds()
                if (holdsResp.isSuccessful) _libbyHolds.value = holdsResp.body() ?: emptyList()
            } catch (e: Exception) {}
        }
    }

    fun setStatusFilter(filter: String) { _statusFilter.value = filter }
    fun setServiceFilter(filter: String) { 
        _serviceFilter.value = filter 
        if (filter.equals("Libby", ignoreCase = true)) loadLibbyData()
    }
    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun addBook(create: BookCreate) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val resp = apiService.addBook(create)
                if (resp.isSuccessful) loadData()
                else _error.value = "Failed to add book: ${resp.code()}"
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateBook(bookId: String, update: BookUpdate) {
        viewModelScope.launch {
            try {
                val resp = apiService.updateBook(bookId, update)
                if (resp.isSuccessful) loadData()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun deleteBook(bookId: String) {
        viewModelScope.launch {
            try {
                val resp = apiService.deleteBook(bookId)
                if (resp.isSuccessful) loadData()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun syncService(service: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val resp = when (service.lowercase()) {
                    "kindle" -> apiService.syncKindle()
                    "google_play" -> apiService.syncGooglePlay()
                    else -> null
                }
                if (resp?.isSuccessful == true) loadData()
            } catch (e: Exception) { _error.value = e.message }
            finally { _isLoading.value = false }
        }
    }

    fun clearError() { _error.value = null }
}
