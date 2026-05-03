package com.riversongai.data.model

import com.google.gson.annotations.SerializedName

data class ModelEntry(
    val provider: String,
    @SerializedName("model_id") val modelId: String,
    @SerializedName("display_name") val displayName: String,
    val available: Boolean = true,
    @SerializedName("is_cloud") val isCloud: Boolean? = null
)

data class ModelCatalog(
    val local: List<ModelEntry> = emptyList(),
    val cloud: List<ModelEntry> = emptyList(),
    @SerializedName("enabled_providers") val enabledProviders: Map<String, Any?> = emptyMap(),
    @SerializedName("ollama_reachable") val ollamaReachable: Boolean = false
)

data class LlmSettings(
    val provider: String = "",
    val model: String = "",
    @SerializedName("cloud_fallback_enabled") val cloudFallbackEnabled: Boolean = false,
    @SerializedName("cloud_fallback_provider") val cloudFallbackProvider: String? = null,
    @SerializedName("cloud_fallback_model") val cloudFallbackModel: String? = null
)

data class DashboardStats(
    @SerializedName("facts_count") val factsCount: Int = 0,
    @SerializedName("summaries_count") val summariesCount: Int = 0,
    @SerializedName("uptime_seconds") val uptimeSeconds: Long = 0L,
    @SerializedName("avg_latency_ms") val avgLatencyMs: Float = 0f
)
