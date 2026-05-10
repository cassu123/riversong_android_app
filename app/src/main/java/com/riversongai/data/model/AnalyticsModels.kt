package com.riversongai.data.model

import com.google.gson.annotations.SerializedName

data class AnalyticsPlatform(
    val key: String,
    val label: String,
    val color: String,
    val metrics: List<String> = emptyList()
)

data class AnalyticsSnapshot(
    val id: String = "",
    val platform: String,
    val date: String,
    val metrics: Map<String, Float> = emptyMap()
)

data class PlatformConfig(
    val enabled: Boolean = true,
    @SerializedName("api_key") val apiKey: String = "",
    @SerializedName("api_secret") val apiSecret: String = "",
    val notes: String = ""
)

data class SystemAnalytics(
    val report: String? = null,
    val generatedAt: String? = null
)

data class PlatformInsight(
    val platform: String,
    val insights: String,
    @SerializedName("generated_at") val generatedAt: String
)
