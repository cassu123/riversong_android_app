package com.riversongai.data.repository

import android.util.Log
import com.riversongai.data.model.LlmSettings
import com.riversongai.data.model.ModelCatalog
import com.riversongai.data.remote.RiverSongApiService

class SettingsRepository(private val apiService: RiverSongApiService) {

    private val tag = "SettingsRepository"

    suspend fun getModels(): Result<ModelCatalog> {
        return try {
            val response = apiService.getModels()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val msg = "Failed to fetch models: ${response.code()}"
                Log.e(tag, msg)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(tag, "getModels exception", e)
            Result.failure(e)
        }
    }

    suspend fun getLlmSettings(): Result<LlmSettings> {
        return try {
            val response = apiService.getLlmSettings()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val msg = "Failed to fetch LLM settings: ${response.code()}"
                Log.e(tag, msg)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(tag, "getLlmSettings exception", e)
            Result.failure(e)
        }
    }

    suspend fun saveLlmSettings(provider: String, modelId: String): Result<Unit> {
        return try {
            val body = mapOf<String, Any?>(
                "provider" to provider,
                "model_id" to modelId
            )
            val response = apiService.saveLlmSettings(body)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val msg = "Failed to save LLM settings: ${response.code()}"
                Log.e(tag, msg)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(tag, "saveLlmSettings exception", e)
            Result.failure(e)
        }
    }

    suspend fun testConnection(): Result<com.riversongai.data.model.DashboardStats> {
        return try {
            val response = apiService.getDashboard()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Connection failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
