package com.riversongai.data.repository

import android.util.Log
import com.riversongai.data.model.Device
import com.riversongai.data.remote.DeviceControlRequest
import com.riversongai.data.remote.RiverSongApiService

class SmartHomeRepository(private val apiService: RiverSongApiService) {

    private val tag = "SmartHomeRepository"

    suspend fun getAllDevices(): Result<List<Device>> {
        return try {
            val response = apiService.getAllDevices()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                Log.e(tag, "Fetch devices failed: ${response.code()} - $error")
                Result.failure(Exception("Failed to fetch devices: ${response.code()} - $error"))
            }
        } catch (e: Exception) {
            Log.e(tag, "Fetch devices exception", e)
            Result.failure(e)
        }
    }

    suspend fun getDeviceById(deviceId: String): Result<Device> {
        return try {
            val response = apiService.getDeviceById(deviceId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                Log.e(tag, "Fetch device $deviceId failed: ${response.code()} - $error")
                Result.failure(Exception("Failed to fetch device: ${response.code()} - $error"))
            }
        } catch (e: Exception) {
            Log.e(tag, "Fetch device exception", e)
            Result.failure(e)
        }
    }

    suspend fun controlDevice(deviceId: String, controlRequest: DeviceControlRequest): Result<Device> {
        return try {
            val response = apiService.controlDevice(deviceId, controlRequest)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                Log.e(tag, "Control device $deviceId failed: ${response.code()} - $error")
                Result.failure(Exception("Failed to control device: ${response.code()} - $error"))
            }
        } catch (e: Exception) {
            Log.e(tag, "Control device exception", e)
            Result.failure(e)
        }
    }
}
