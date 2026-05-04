package com.riversongai.data.repository

import com.riversongai.data.model.CommerceWorkspace
import com.riversongai.data.model.CreateProduct
import com.riversongai.data.model.CreateWorkspace
import com.riversongai.data.model.Product
import com.riversongai.data.model.StockAdjust
import com.riversongai.data.remote.RiverSongApiService

class CommerceRepository(private val api: RiverSongApiService) {

    suspend fun getWorkspaces(): Result<List<CommerceWorkspace>> = runCatching {
        val r = api.getWorkspaces()
        if (r.isSuccessful) r.body()!! else error(r.code().toString())
    }

    suspend fun createWorkspace(name: String): Result<CommerceWorkspace> = runCatching {
        val r = api.createWorkspace(CreateWorkspace(name))
        if (r.isSuccessful) r.body()!! else error(r.errorBody()?.string() ?: "Failed")
    }

    suspend fun getProducts(workspaceId: String): Result<List<Product>> = runCatching {
        val r = api.getProducts(workspaceId)
        if (r.isSuccessful) r.body()!! else error(r.code().toString())
    }

    suspend fun createProduct(workspaceId: String, body: CreateProduct): Result<Product> = runCatching {
        val r = api.createProduct(workspaceId, body)
        if (r.isSuccessful) r.body()!! else error(r.errorBody()?.string() ?: "Failed")
    }

    suspend fun updateProduct(productId: String, body: CreateProduct): Result<Product> = runCatching {
        val r = api.updateProduct(productId, body)
        if (r.isSuccessful) r.body()!! else error(r.errorBody()?.string() ?: "Failed")
    }

    suspend fun deleteProduct(productId: String): Result<Unit> = runCatching {
        api.deleteProduct(productId)
    }

    suspend fun adjustStock(productId: String, delta: Int): Result<Product> = runCatching {
        val r = api.adjustStock(productId, StockAdjust(delta))
        if (r.isSuccessful) r.body()!! else error(r.errorBody()?.string() ?: "Failed")
    }
}
