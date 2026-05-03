package com.riversongai.data.model

import com.google.gson.annotations.SerializedName

data class MemoryPreference(
    val category: String,
    val value: String,
    val confidence: Float
)

data class MemorySummary(
    val id: String,
    val summary: String,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("expires_at") val expiresAt: Long,
    val ttl: String
)

data class MemoryStats(
    val factsCount: Int,
    val prefsCount: Int,
    val sessionsCount: Int
)
