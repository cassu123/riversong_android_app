package com.riversongai.data.repository

import com.riversongai.data.model.*
import com.riversongai.data.remote.RiverSongApiService
import okhttp3.MultipartBody

class MaintenanceRepository(private val api: RiverSongApiService) {

    suspend fun getVehicles(): Result<List<Vehicle>> = runCatching {
        val r = api.getVehicles()
        if (r.isSuccessful) r.body()!! else error(r.code().toString())
    }

    suspend fun createVehicle(body: CreateVehicle): Result<Vehicle> = runCatching {
        val r = api.createVehicle(body)
        if (r.isSuccessful) r.body()!! else error(r.errorBody()?.string() ?: "Failed")
    }

    suspend fun deleteVehicle(vehicleId: String): Result<Unit> = runCatching {
        val r = api.deleteVehicle(vehicleId)
        if (r.isSuccessful) Unit else error(r.code().toString())
    }

    suspend fun getServiceLogs(vehicleId: String): Result<List<ServiceLog>> = runCatching {
        val r = api.getServiceLogs(vehicleId)
        if (r.isSuccessful) r.body()!! else error(r.code().toString())
    }

    suspend fun createServiceLog(vehicleId: String, body: CreateServiceLog): Result<ServiceLog> = runCatching {
        val r = api.createServiceLog(vehicleId, body)
        if (r.isSuccessful) r.body()!! else error(r.errorBody()?.string() ?: "Failed")
    }

    suspend fun getAssignments(vehicleId: String): Result<List<VehicleAssignment>> = runCatching {
        val r = api.getVehicleAssignments(vehicleId)
        if (r.isSuccessful) r.body()!! else error(r.code().toString())
    }
}
