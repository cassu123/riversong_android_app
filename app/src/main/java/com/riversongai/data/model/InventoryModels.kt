package com.riversongai.data.model

data class InventoryHome(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val itemCount: Int = 0,
    val collaboratorCount: Int = 0,
    val defaultQrStandard: String = "QR",
)

data class InventoryItem(
    val id: String = "",
    val ein: String = "",
    val name: String = "",
    val category: String = "Other",
    val location: String = "",
    val manufacturer: String = "",
    val modelNumber: String = "",
    val serialNumber: String = "",
    val quantity: Int = 1,
    val description: String = "",
    val purchasePrice: Double? = null,
    val replacementCost: Double? = null,
    val assetStatus: String = "Serviceable",
    val isInsured: Boolean = false,
)

data class CreateInventoryHome(
    val name: String,
    val description: String = "",
)

data class CreateInventoryItem(
    val name: String,
    val category: String = "Other",
    val location: String = "",
    val assetStatus: String = "Serviceable",
    val quantity: Int = 1,
    val description: String = "",
    val replacementCost: Double? = null,
    val manufacturer: String = "",
    val modelNumber: String = "",
    val serialNumber: String = "",
)
