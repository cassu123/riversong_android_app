package com.riversongai.data.repository

import com.riversongai.data.model.CreateInventoryHome
import com.riversongai.data.model.CreateInventoryItem
import com.riversongai.data.model.InventoryHome
import com.riversongai.data.model.InventoryItem
import com.riversongai.data.remote.RiverSongApiService

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
        api.deleteInventoryHome(homeId)
    }

    suspend fun getItems(homeId: String): Result<List<InventoryItem>> = runCatching {
        val r = api.getInventoryItems(homeId)
        if (r.isSuccessful) r.body()!! else error(r.code().toString())
    }

    suspend fun createItem(homeId: String, body: CreateInventoryItem): Result<InventoryItem> = runCatching {
        val r = api.createInventoryItem(homeId, body)
        if (r.isSuccessful) r.body()!! else error(r.errorBody()?.string() ?: "Failed")
    }

    suspend fun updateItem(itemId: String, body: CreateInventoryItem): Result<InventoryItem> = runCatching {
        val r = api.updateInventoryItem(itemId, body)
        if (r.isSuccessful) r.body()!! else error(r.errorBody()?.string() ?: "Failed")
    }

    suspend fun deleteItem(itemId: String): Result<Unit> = runCatching {
        api.deleteInventoryItem(itemId)
    }
}
