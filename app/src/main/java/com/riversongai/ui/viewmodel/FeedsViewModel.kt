package com.riversongai.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riversongai.data.model.FeedPreferences
import com.riversongai.data.model.NewsArticle
import com.riversongai.data.model.StockQuote
import com.riversongai.data.model.WeatherData
import com.riversongai.data.repository.FeedsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class FeedsViewModel(private val feedsRepository: FeedsRepository) : ViewModel() {

    private val _news = MutableLiveData<List<NewsArticle>>(emptyList())
    val news: LiveData<List<NewsArticle>> = _news

    private val _weather = MutableLiveData<WeatherData?>()
    val weather: LiveData<WeatherData?> = _weather

    private val _stocks = MutableLiveData<List<StockQuote>>(emptyList())
    val stocks: LiveData<List<StockQuote>> = _stocks

    private val _preferences = MutableLiveData<FeedPreferences?>()
    val preferences: LiveData<FeedPreferences?> = _preferences

    private val _isLoadingNews = MutableLiveData(false)
    val isLoadingNews: LiveData<Boolean> = _isLoadingNews

    private val _isLoadingWeather = MutableLiveData(false)
    val isLoadingWeather: LiveData<Boolean> = _isLoadingWeather

    private val _isLoadingStocks = MutableLiveData(false)
    val isLoadingStocks: LiveData<Boolean> = _isLoadingStocks

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _saveResult = MutableLiveData<String?>()
    val saveResult: LiveData<String?> = _saveResult

    private val _newsCategory = MutableLiveData("All")
    val newsCategory: LiveData<String> = _newsCategory

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            val newsJob = async { loadNews() }
            val weatherJob = async { loadWeather() }
            val stocksJob = async { loadStocks() }
            newsJob.await()
            weatherJob.await()
            stocksJob.await()
        }
    }

    fun loadNews() {
        viewModelScope.launch {
            _isLoadingNews.value = true
            feedsRepository.getNews(_newsCategory.value).fold(
                onSuccess = { _news.value = it },
                onFailure = { _error.value = it.message }
            )
            _isLoadingNews.value = false
        }
    }

    fun setNewsCategory(category: String) {
        _newsCategory.value = category
        loadNews()
    }

    fun loadWeather() {
        viewModelScope.launch {
            _isLoadingWeather.value = true
            feedsRepository.getWeather().fold(
                onSuccess = { _weather.value = it },
                onFailure = { _weather.value = null }
            )
            _isLoadingWeather.value = false
        }
    }

    fun saveWeatherLocation(location: String) {
        viewModelScope.launch {
            _isLoadingWeather.value = true
            feedsRepository.saveWeatherLocation(location).fold(
                onSuccess = {
                    _saveResult.value = "Location updated"
                    loadWeather()
                },
                onFailure = { _error.value = it.message }
            )
            _isLoadingWeather.value = false
        }
    }

    fun loadStocks() {
        viewModelScope.launch {
            _isLoadingStocks.value = true
            feedsRepository.getStocks().fold(
                onSuccess = { _stocks.value = it },
                onFailure = { /* non-fatal */ }
            )
            _isLoadingStocks.value = false
        }
    }

    fun loadPreferences() {
        viewModelScope.launch {
            feedsRepository.getFeedPreferences().fold(
                onSuccess = { _preferences.value = it },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun savePreferences(prefs: FeedPreferences) {
        viewModelScope.launch {
            feedsRepository.saveFeedPreferences(prefs).fold(
                onSuccess = {
                    _preferences.value = prefs
                    _saveResult.value = "Preferences saved"
                    loadAll()
                },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun clearError() { _error.value = null }
    fun clearSaveResult() { _saveResult.value = null }
}
