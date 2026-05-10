package com.riversongai.data.repository

import com.riversongai.data.model.*
import com.riversongai.data.remote.RiverSongApiService

class FeedsRepository(private val api: RiverSongApiService) {

    suspend fun getNews(category: String?): Result<List<NewsArticle>> = try {
        val cat = if (category == "All") null else category?.lowercase()
        val response = api.getNews(cat)
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception("Error ${response.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getWeather(): Result<WeatherData> = try {
        val response = api.getWeather()
        if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
        else Result.failure(Exception("Error ${response.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getStocks(): Result<List<StockQuote>> = try {
        val response = api.getStocks()
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception("Error ${response.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getStockChart(ticker: String): Result<List<StockChartEntry>> = try {
        val response = api.getStockChart(ticker)
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception("Error ${response.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getFeedPreferences(): Result<FeedPreferences> = try {
        val response = api.getFeedPreferences()
        if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
        else Result.failure(Exception("Error ${response.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun saveFeedPreferences(prefs: FeedPreferences): Result<Unit> = try {
        val response = api.saveFeedPreferences(prefs)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Error ${response.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun saveWeatherLocation(lat: Double, lon: Double): Result<Unit> = try {
        val response = api.saveFeedPreferences(FeedPreferences(weatherLat = lat, weatherLon = lon))
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Error ${response.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun saveWeatherUnit(unit: String): Result<Unit> = try {
        val response = api.saveFeedPreferences(FeedPreferences(weatherUnit = unit))
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Error ${response.code()}"))
    } catch (e: Exception) { Result.failure(e) }
}
