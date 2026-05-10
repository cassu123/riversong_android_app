package com.riversongai.data.model

import com.google.gson.annotations.SerializedName

data class AnalyticsSnapshot(
    val id: String,
    val platform: String,
    val date: String,
    val metrics: AnalyticsMetrics
)

data class AnalyticsMetrics(
    val followers: Int? = null,
    val views: Int? = null,
    val revenue: Double? = null,
    val orders: Int? = null
)

data class PlatformSummary(
    val summary: String
)

data class SnapshotCreate(
    val platform: String,
    val date: String, // YYYY-MM-DD
    val metrics: Map<String, Double>
)
