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
    val imageBase64: String? = null
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
    val imageBase64: String? = null
)

data class StockAdjust(val delta: Int)

data class Supplier(
    val id: String = "",
    val name: String = "",
    val contactName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val categories: List<String> = emptyList()
)

data class Customer(
    val id: String = "",
    val name: String = "",
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val tags: List<String> = emptyList(),
    val totalSpent: Double = 0.0
)

data class Sale(
    val id: String = "",
    val customerId: String? = null,
    val items: List<SaleItem> = emptyList(),
    val subtotal: Double = 0.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val status: String = "completed",
    val createdAt: String = ""
)

data class SaleItem(
    val productId: String,
    val quantity: Int,
    val unitPrice: Double
)

data class WorkspaceMember(
    val id: String = "",
    val userId: String = "",
    val workspaceId: String = "",
    val role: String = "member",
    val email: String = "",
    val name: String = ""
)
