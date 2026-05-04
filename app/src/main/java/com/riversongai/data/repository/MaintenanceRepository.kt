package com.riversongai.data.repository

import com.riversongai.data.model.CreateServiceLog
import com.riversongai.data.model.CreateVehicle
import com.riversongai.data.model.ServiceCheckpoint
import com.riversongai.data.model.ServiceLog
import com.riversongai.data.model.Vehicle
import com.riversongai.data.remote.RiverSongApiService

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
        api.deleteVehicle(vehicleId)
    }

    suspend fun getCheckpoints(vehicleId: String): Result<List<ServiceCheckpoint>> = runCatching {
        val r = api.getServiceCheckpoints(vehicleId)
        if (r.isSuccessful) r.body()!! else error(r.code().toString())
    }

    suspend fun getServiceLogs(vehicleId: String): Result<List<ServiceLog>> = runCatching {
        val r = api.getServiceLogs(vehicleId)
        if (r.isSuccessful) r.body()!! else error(r.code().toString())
    }

    suspend fun createServiceLog(vehicleId: String, body: CreateServiceLog): Result<ServiceLog> = runCatching {
        val r = api.createServiceLog(vehicleId, body)
        if (r.isSuccessful) r.body()!! else error(r.errorBody()?.string() ?: "Failed")
    }
}
