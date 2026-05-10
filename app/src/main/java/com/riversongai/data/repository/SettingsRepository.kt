package com.riversongai.data.repository

import android.util.Log
import com.riversongai.data.model.LlmSettings
import com.riversongai.data.model.ModelCatalog
import com.riversongai.data.remote.RiverSongApiService

class SettingsRepository(private val apiService: RiverSongApiService) {

    private val tag = "SettingsRepository"

    suspend fun getModelCatalog(): Result<ModelCatalog> {
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

    suspend fun saveLlmSettings(
        provider: String,
        modelId: String,
        fallbackEnabled: Boolean? = null,
        fallbackProvider: String? = null,
        fallbackModel: String? = null
    ): Result<Unit> {
        return try {
            val body = LlmSettings(
                provider = provider,
                model = modelId,
                cloudFallbackEnabled = fallbackEnabled ?: false,
                cloudFallbackProvider = fallbackProvider,
                cloudFallbackModel = fallbackModel
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

    suspend fun getVoices() = try {
        val response = apiService.getVoices()
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception("Error: ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getMemoryTtl() = try {
        val response = apiService.getMemoryTtl()
        if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
        else Result.failure(Exception("Error: ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun saveMemoryTtl(settings: com.riversongai.data.model.MemoryTtlSettings) = try {
        val response = apiService.updateMemoryTtl(settings)
        if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
        else Result.failure(Exception("Error: ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getN8nSettings(): Result<com.riversongai.data.model.N8nSettings> {
        return try {
            val response = apiService.getOrchestrationSettings()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch n8n settings: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveN8nSettings(settings: com.riversongai.data.model.N8nSettings): Result<Unit> {
        return try {
            val response = apiService.saveOrchestrationSettings(settings)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to save n8n settings: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun testVoice(voiceId: String) = try {
        val response = apiService.testVoice(mapOf("voice_id" to voiceId))
        if (response.isSuccessful && response.body() != null) Result.success(response.body()!!.bytes())
        else Result.failure(Exception("Error: ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
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
