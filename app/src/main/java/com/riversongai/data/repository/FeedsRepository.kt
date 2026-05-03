package com.riversongai.data.repository

import android.util.Log
import com.riversongai.data.model.FeedPreferences
import com.riversongai.data.model.NewsArticle
import com.riversongai.data.model.StockQuote
import com.riversongai.data.model.WeatherData
import com.riversongai.data.remote.RiverSongApiService

class FeedsRepository(private val apiService: RiverSongApiService) {

    private val tag = "FeedsRepository"

    suspend fun getNews(category: String? = null): Result<List<NewsArticle>> {
        return try {
            val response = apiService.getNews(category)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val msg = "Failed to fetch news: ${response.code()}"
                Log.e(tag, msg)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(tag, "getNews exception", e)
            Result.failure(e)
        }
    }

    suspend fun getWeather(): Result<WeatherData> {
        return try {
            val response = apiService.getWeather()
            when {
                response.code() == 404 -> {
                    Result.failure(Exception("No location set. Configure in Feed Settings."))
                }
                response.isSuccessful && response.body() != null -> {
                    Result.success(response.body()!!)
                }
                else -> {
                    val msg = "Failed to fetch weather: ${response.code()}"
                    Log.e(tag, msg)
                    Result.failure(Exception(msg))
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "getWeather exception", e)
            Result.failure(e)
        }
    }

    suspend fun getStocks(): Result<List<StockQuote>> {
        return try {
            val response = apiService.getStocks()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val msg = "Failed to fetch stocks: ${response.code()}"
                Log.e(tag, msg)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(tag, "getStocks exception", e)
            Result.failure(e)
        }
    }

    suspend fun getFeedPreferences(): Result<FeedPreferences> {
        return try {
            val response = apiService.getFeedPreferences()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val msg = "Failed to fetch feed preferences: ${response.code()}"
                Log.e(tag, msg)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(tag, "getFeedPreferences exception", e)
            Result.failure(e)
        }
    }

    suspend fun saveFeedPreferences(prefs: FeedPreferences): Result<Unit> {
        return try {
            val response = apiService.saveFeedPreferences(prefs)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val msg = "Failed to save feed preferences: ${response.code()}"
                Log.e(tag, msg)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(tag, "saveFeedPreferences exception", e)
            Result.failure(e)
        }
    }

    suspend fun saveWeatherLocation(location: String): Result<Unit> {
        return try {
            val response = apiService.saveSettings(mapOf("weather_location" to location))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
