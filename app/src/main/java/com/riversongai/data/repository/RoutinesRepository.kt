package com.riversongai.data.repository

import android.util.Log
import com.riversongai.data.model.Routine
import com.riversongai.data.model.RoutineCreate
import com.riversongai.data.model.RoutineRunResponse
import com.riversongai.data.remote.RiverSongApiService

class RoutinesRepository(private val apiService: RiverSongApiService) {

    private val tag = "RoutinesRepository"

    suspend fun getRoutines(): Result<List<Routine>> {
        return try {
            val response = apiService.getRoutines()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val msg = "Failed to fetch routines: ${response.code()}"
                Log.e(tag, msg)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(tag, "getRoutines exception", e)
            Result.failure(e)
        }
    }

    suspend fun createRoutine(create: RoutineCreate): Result<Routine> {
        return try {
            val response = apiService.createRoutine(create)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val msg = "Failed to create routine: ${response.code()}"
                Log.e(tag, msg)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(tag, "createRoutine exception", e)
            Result.failure(e)
        }
    }

    suspend fun toggleRoutine(routineId: String, enabled: Boolean): Result<Routine> {
        return try {
            val response = apiService.updateRoutine(routineId, mapOf("enabled" to enabled))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val msg = "Failed to toggle routine: ${response.code()}"
                Log.e(tag, msg)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(tag, "toggleRoutine exception", e)
            Result.failure(e)
        }
    }

    suspend fun deleteRoutine(routineId: String): Result<Unit> {
        return try {
            val response = apiService.deleteRoutine(routineId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val msg = "Failed to delete routine: ${response.code()}"
                Log.e(tag, msg)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(tag, "deleteRoutine exception", e)
            Result.failure(e)
        }
    }

    suspend fun runRoutine(routineId: String): Result<RoutineRunResponse> {
        return try {
            val response = apiService.runRoutine(routineId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val msg = "Failed to run routine: ${response.code()}"
                Log.e(tag, msg)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(tag, "runRoutine exception", e)
            Result.failure(e)
        }
    }
}
