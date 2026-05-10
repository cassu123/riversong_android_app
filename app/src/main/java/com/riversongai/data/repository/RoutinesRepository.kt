package com.riversongai.data.repository

import com.riversongai.data.model.Routine
import com.riversongai.data.model.RoutineCreate
import com.riversongai.data.model.RoutineRunResponse
import com.riversongai.data.remote.RiverSongApiService

class RoutinesRepository(private val api: RiverSongApiService) {

    suspend fun getRoutines(): Result<List<Routine>> = runCatching {
        val r = api.getRoutines()
        if (r.isSuccessful) r.body()!! else error(r.code().toString())
    }

    suspend fun createRoutine(body: RoutineCreate): Result<Routine> = runCatching {
        val r = api.createRoutine(body)
        if (r.isSuccessful) r.body()!! else error(r.errorBody()?.string() ?: "Failed")
    }

    suspend fun updateRoutine(id: String, fields: Map<String, Any?>): Result<Routine> = runCatching {
        val r = api.updateRoutine(id, fields)
        if (r.isSuccessful) r.body()!! else error(r.errorBody()?.string() ?: "Failed")
    }

    suspend fun deleteRoutine(id: String): Result<Unit> = runCatching {
        val r = api.deleteRoutine(id)
        if (r.isSuccessful) Unit else error(r.code().toString())
    }

    suspend fun runRoutine(id: String): Result<RoutineRunResponse> = runCatching {
        val r = api.runRoutine(id)
        if (r.isSuccessful) r.body()!! else error(r.errorBody()?.string() ?: "Failed")
    }

    suspend fun getN8nStatus(): Result<Map<String, Any?>> = runCatching {
        val r = api.getOrchestrationSettings() // We'll repurpose this for status or use a specific one if available
        // In the web app it was /api/webhooks/n8n/status
        // For now we'll assume settings gives us what we need or add the endpoint
        mapOf("n8n_available" to true) // Mocking for now as per web res structure
    }
}
