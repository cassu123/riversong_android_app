package com.riversongai.data.model

import com.google.gson.annotations.SerializedName

data class Fact(
    val id: String,
    val key: String,
    val value: String,
    val source: String = "manual",
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class MemoryPreference(
    val id: String,
    val category: String,
    val value: String,
    val confidence: String, // high, medium, low
    @SerializedName("last_updated") val lastUpdated: String? = null
)

data class MemorySummary(
    val id: String,
    val summary: String,
    @SerializedName("ttl_setting") val ttlSetting: String,
    @SerializedName("expires_at") val expiresAt: String? = null,
    @SerializedName("reference_count") val referenceCount: Int = 0,
    @SerializedName("created_at") val createdAt: String? = null
)

data class MemoryStats(
    val factsCount: Int,
    val prefsCount: Int,
    val sessionsCount: Int
)

data class FactCreate(
    val key: String,
    val value: String
)
