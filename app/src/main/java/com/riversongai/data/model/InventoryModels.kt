package com.riversongai.data.model

import com.google.gson.annotations.SerializedName

data class InventoryHome(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    @SerializedName("owner_id") val ownerId: String = "",
    @SerializedName("item_count") val itemCount: Int = 0,
    @SerializedName("collaborator_count") val collaboratorCount: Int = 0,
    @SerializedName("qr_standard") val defaultQrStandard: String = "qr",
)

data class InventoryItem(
    val id: String = "",
    val ein: String = "",
    @SerializedName("home_id") val homeId: String = "",
    val name: String = "",
    val category: String = "Other",
    val location: String = "",
    val manufacturer: String = "",
    @SerializedName("model_number") val modelNumber: String = "",
    @SerializedName("serial_number") val serialNumber: String = "",
    val quantity: Int = 1,
    val description: String = "",
    @SerializedName("purchase_price") val purchasePrice: Double? = null,
    @SerializedName("replacement_cost") val replacementCost: Double? = null,
    @SerializedName("asset_status") val assetStatus: String = "Serviceable",
    @SerializedName("is_insured") val isInsured: Boolean = false,
    @SerializedName("current_custodian") val currentCustodian: Map<String, String>? = null,
    @SerializedName("qr_standard") val qrStandard: String = "qr"
)

data class ItemAttachment(
    val id: String,
    @SerializedName("item_id") val itemId: String,
    @SerializedName("original_filename") val originalFilename: String,
    @SerializedName("file_size") val fileSize: Long,
    @SerializedName("mime_type") val mimeType: String,
    @SerializedName("created_at") val createdAt: String
)

data class InventoryAudit(
    val id: String,
    @SerializedName("home_id") val homeId: String,
    val status: String,
    @SerializedName("total_items") val totalItems: Int,
    @SerializedName("scanned_count") val scannedCount: Int,
    val scanned: List<Map<String, String>> = emptyList(),
    val missing: List<Map<String, String>> = emptyList(),
    val notes: String = "",
    @SerializedName("started_at") val startedAt: String? = null,
    @SerializedName("completed_at") val completedAt: String? = null
)

data class CreateInventoryHome(
    val name: String,
    val description: String = "",
    @SerializedName("qr_standard") val qrStandard: String = "qr"
)

data class CreateInventoryItem(
    val name: String,
    val category: String = "Other",
    val location: String = "",
    @SerializedName("asset_status") val assetStatus: String = "Serviceable",
    val quantity: Int = 1,
    val description: String = "",
    @SerializedName("replacement_cost") val replacementCost: Double? = null,
    val manufacturer: String = "",
    @SerializedName("model_number") val modelNumber: String = "",
    @SerializedName("serial_number") val serialNumber: String = "",
    @SerializedName("is_insured") val isInsured: Boolean = false
)
