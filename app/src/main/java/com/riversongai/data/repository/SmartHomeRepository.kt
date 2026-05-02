package com.riversongai.data.repository

import android.util.Log
import com.riversongai.data.model.Device
import com.riversongai.data.remote.HomeActionRequest
import com.riversongai.data.remote.RiverSongApiService

class SmartHomeRepository(private val apiService: RiverSongApiService) {

    private val tag = "SmartHomeRepository"

    suspend fun getAllDevices(): Result<List<Device>> {
        return try {
            val response = apiService.getDevices()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val error = response.errorBody()?.string() ?: "Error ${response.code()}"
                Log.e(tag, "Fetch devices failed: $error")
                Result.failure(Exception("Failed to fetch devices"))
            }
        } catch (e: Exception) {
            Log.e(tag, "Fetch devices exception", e)
            Result.failure(e)
        }
    }

    suspend fun controlDevice(
        entityId: String,
        action: String,
        brightnessPct: Int? = null,
        temperature: Float? = null
    ): Result<Unit> {
        return try {
            val response = apiService.callAction(
                HomeActionRequest(entityId, action, brightnessPct, temperature)
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val error = response.errorBody()?.string() ?: "Error ${response.code()}"
                Log.e(tag, "Control device failed: $error")
                Result.failure(Exception("Failed to control device"))
            }
        } catch (e: Exception) {
            Log.e(tag, "Control device exception", e)
            Result.failure(e)
        }
    }
}
