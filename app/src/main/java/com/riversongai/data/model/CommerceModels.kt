package com.riversongai.data.model

data class CommerceWorkspace(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val currency: String = "USD",
    val taxRate: Double = 0.0,
    val productCount: Int = 0,
    val customerCount: Int = 0,
)

data class Product(
    val id: String = "",
    val sku: String = "",
    val name: String = "",
    val category: String = "Other",
    val description: String = "",
    val stockQty: Int = 0,
    val lowStock: Int = 5,
    val unitPrice: Double = 0.0,
    val costPrice: Double = 0.0,
    val isActive: Boolean = true,
)

data class CreateWorkspace(
    val name: String,
    val description: String = "",
    val currency: String = "USD",
    val taxRate: Double = 0.0,
)

data class CreateProduct(
    val sku: String,
    val name: String,
    val category: String = "Other",
    val description: String = "",
    val stockQty: Int = 0,
    val lowStock: Int = 5,
    val unitPrice: Double = 0.0,
    val costPrice: Double = 0.0,
)

data class StockAdjust(val delta: Int)
