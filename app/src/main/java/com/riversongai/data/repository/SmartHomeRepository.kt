// app/src/main/java/com/riversongai/data/repository/SmartHomeRepository.kt
//
// This file defines the repository for managing smart home device data.
// It acts as a single source of truth, abstracting data origins (like the network API)
// and providing a clean API for the rest of the application to interact with device information.

package com.riversongai.data.repository

import com.riversongai.data.model.Device
import com.riversongai.data.remote.DeviceControlRequest
import com.riversongai.data.remote.RiverSongApiService
import android.util.Log

// Section: SmartHomeRepository Class Definition
// This class is responsible for handling all data operations related to smart home devices.
// It mediates between the RiverSongApiService (network source) and the calling components (e.g., ViewModels).
class SmartHomeRepository(private val apiService: RiverSongApiService) {

    // Section: Constants
    // Tag for logging within this repository.
    private val TAG = "SmartHomeRepository"

    // Section: Fetch All Devices
    // Retrieves a list of all smart home devices from the backend API.
    // It handles the network call and returns a Result encapsulating either success (List of Device) or failure.
    suspend fun getAllDevices(authToken: String): Result<List<Device>> {
        return try {
            val response = apiService.getAllDevices(authToken)
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Successfully fetched all devices.")
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Failed to fetch all devices: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to fetch devices: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching all devices: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Section: Get Device by ID
    // Retrieves a specific smart home device by its unique identifier from the backend API.
    suspend fun getDeviceById(authToken: String, deviceId: String): Result<Device> {
        return try {
            val response = apiService.getDeviceById(authToken, deviceId)
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Successfully fetched device: $deviceId")
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Failed to fetch device $deviceId: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to fetch device $deviceId: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching device $deviceId: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Section: Control Device
    // Sends a control command to a specific device via the backend API.
    // This could be to turn it on/off, set a temperature, change brightness, etc.
    suspend fun controlDevice(
        authToken: String,
        deviceId: String,
        controlRequest: DeviceControlRequest
    ): Result<Device> {
        return try {
            val response = apiService.controlDevice(authToken, deviceId, controlRequest)
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Successfully sent control command to device: $deviceId")
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Failed to control device $deviceId: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to control device $deviceId: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception controlling device $deviceId: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Section: Example of Data Caching (Placeholder)
    // This method demonstrates how you might add caching logic in the future.
    // For a real app, you would integrate with a local database (e.g., Room) or in-memory cache.
    // suspend fun getCachedDevices(): List<Device> {
    //     // Logic to retrieve devices from local database or cache
    //     return emptyList() // Placeholder
    // }

    // Section: Example of Local Device Management (Placeholder)
    // This shows how the repository could handle operations that don't need a network call.
    // fun updateDeviceStatusLocally(deviceId: String, newStatus: String) {
    //     // Logic to update device status in a local cache or database
    //     Log.d(TAG, "Updated device $deviceId status locally to $newStatus")
    // }
}