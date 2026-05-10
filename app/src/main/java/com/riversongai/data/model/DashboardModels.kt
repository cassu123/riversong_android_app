package com.riversongai.data.model

import com.google.gson.annotations.SerializedName

data class DashboardStats(
    val status: String = "operational",
    @SerializedName("latency_ms") val latencyMs: Int = 0,
    val uptime: String = "",
    @SerializedName("started_at") val startedAt: String = "",
    val memory: MemoryCounts = MemoryCounts()
)

data class MemoryCounts(
    val facts: Int = 0,
    val summaries: Int = 0,
    val prefs: Int = 0,
    val today: Int = 0
)

data class HomeActionRequest(
    @SerializedName("entity_id") val entityId: String,
    val action: String,
    @SerializedName("brightness_pct") val brightnessPct: Int? = null,
    val temperature: Float? = null
)

data class ChatSession(
    val time: String,
    val date: String,
    val text: String,
    val count: Int,
    val model: String
)
