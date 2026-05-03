package com.riversongai.data.repository

import android.util.Log
import com.riversongai.data.model.Fact
import com.riversongai.data.model.FactCreate
import com.riversongai.data.remote.RiverSongApiService

class MemoryRepository(private val apiService: RiverSongApiService) {

    private val tag = "MemoryRepository"

    suspend fun getFacts(): Result<List<Fact>> {
        return try {
            val response = apiService.getFacts()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val msg = "Failed to fetch facts: ${response.code()}"
                Log.e(tag, msg)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(tag, "getFacts exception", e)
            Result.failure(e)
        }
    }

    suspend fun createFact(key: String, value: String): Result<Fact> {
        return try {
            val response = apiService.createFact(FactCreate(key, value))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val msg = "Failed to create fact: ${response.code()}"
                Log.e(tag, msg)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(tag, "createFact exception", e)
            Result.failure(e)
        }
    }

    suspend fun deleteFact(factId: String): Result<Unit> {
        return try {
            val response = apiService.deleteFact(factId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val msg = "Failed to delete fact: ${response.code()}"
                Log.e(tag, msg)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(tag, "deleteFact exception", e)
            Result.failure(e)
        }
    }

    suspend fun getPreferences() = try {
        val response = apiService.getMemoryPreferences()
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception("Error: ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getSummaries() = try {
        val response = apiService.getMemorySummaries()
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception("Error: ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
