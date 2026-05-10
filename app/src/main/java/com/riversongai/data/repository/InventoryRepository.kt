package com.riversongai.data.repository

import com.riversongai.data.model.*
import com.riversongai.data.remote.RiverSongApiService
import okhttp3.MultipartBody
import java.io.File

class InventoryRepository(private val api: RiverSongApiService) {

    suspend fun getHomes(): Result<List<InventoryHome>> = runCatching {
        val r = api.getInventoryHomes()
        if (r.isSuccessful) r.body()!! else error(r.code().toString())
    }

    suspend fun createHome(name: String, description: String = ""): Result<InventoryHome> = runCatching {
        val r = api.createInventoryHome(CreateInventoryHome(name, description))
        if (r.isSuccessful) r.body()!! else error(r.errorBody()?.string() ?: "Failed")
    }

    suspend fun deleteHome(homeId: String): Result<Unit> = runCatching {
        val r = api.deleteInventoryHome(homeId)
        if (r.isSuccessful) Unit else error(r.code().toString())
    }

    suspend fun getItems(homeId: String): Result<List<InventoryItem>> = runCatching {
        val r = api.getInventoryItems(homeId)
        if (r.isSuccessful) r.body()!! else error(r.code().toString())
    }

    suspend fun createItem(homeId: String, body: CreateInventoryItem): Result<InventoryItem> = runCatching {
        val r = api.createInventoryItem(homeId, body)
        if (r.isSuccessful) r.body()!! else error(r.errorBody()?.string() ?: "Failed")
    }

    suspend fun updateItem(itemId: String, fields: Map<String, Any?>): Result<InventoryItem> = runCatching {
        val r = api.updateInventoryItem(itemId, fields)
        if (r.isSuccessful) r.body()!! else error(r.errorBody()?.string() ?: "Failed")
    }

    suspend fun deleteItem(itemId: String): Result<Unit> = runCatching {
        val r = api.deleteInventoryItem(itemId)
        if (r.isSuccessful) Unit else error(r.code().toString())
    }

    suspend fun getAttachments(itemId: String): Result<List<ItemAttachment>> = runCatching {
        val r = api.getItemAttachments(itemId)
        if (r.isSuccessful) r.body()!! else error(r.code().toString())
    }

    suspend fun uploadAttachment(itemId: String, file: MultipartBody.Part): Result<ItemAttachment> = runCatching {
        val r = api.uploadItemAttachment(itemId, file)
        if (r.isSuccessful) r.body()!! else error(r.errorBody()?.string() ?: "Failed")
    }

    suspend fun issueItem(itemId: String, email: String): Result<InventoryItem> = runCatching {
        val r = api.issueInventoryItem(itemId, mapOf("collaborator_email" to email))
        if (r.isSuccessful) r.body()!! else error(r.errorBody()?.string() ?: "Failed")
    }

    suspend fun returnItem(itemId: String): Result<InventoryItem> = runCatching {
        val r = api.returnInventoryItem(itemId)
        if (r.isSuccessful) r.body()!! else error(r.errorBody()?.string() ?: "Failed")
    }

    suspend fun getActiveAudit(homeId: String): Result<InventoryAudit?> = runCatching {
        val r = api.getActiveAudit(homeId)
        if (r.isSuccessful) r.body() else if (r.code() == 404) null else error(r.code().toString())
    }

    suspend fun startAudit(homeId: String): Result<InventoryAudit> = runCatching {
        val r = api.startInventoryAudit(homeId)
        if (r.isSuccessful) r.body()!! else error(r.errorBody()?.string() ?: "Failed")
    }

    suspend fun analyzePhoto(file: MultipartBody.Part): Result<Map<String, String>> = runCatching {
        val r = api.analyzeInventoryPhoto(file)
        if (r.isSuccessful) r.body()!! else error(r.errorBody()?.string() ?: "Failed")
    }
}
