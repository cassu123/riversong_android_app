package com.riversongai.data.model

import com.google.gson.annotations.SerializedName

data class ModelEntry(
    val provider: String,
    @SerializedName("model_id") val modelId: String,
    @SerializedName("display_name") val displayName: String,
    val available: Boolean = true,
    @SerializedName("is_cloud") val isCloud: Boolean? = null,
    val tags: String? = null,
    @SerializedName("size_mb") val sizeMb: Int? = null,
    @SerializedName("vram_gb") val vramGb: Float? = null,
    @SerializedName("cost_per_1k_input_usd") val costInputUsd: Double? = null,
    @SerializedName("cost_per_1k_output_usd") val costOutputUsd: Double? = null,
    val notes: String? = null
)

data class LlmSettings(
    val provider: String = "",
    val model: String = "",
    @SerializedName("cloud_fallback_enabled") val cloudFallbackEnabled: Boolean = false,
    @SerializedName("cloud_fallback_provider") val cloudFallbackProvider: String? = null,
    @SerializedName("cloud_fallback_model") val cloudFallbackModel: String? = null
)

data class ModelCatalog(
    val local: List<ModelEntry> = emptyList(),
    val cloud: List<ModelEntry> = emptyList(),
    @SerializedName("enabled_providers") val enabledProviders: Map<String, Boolean> = emptyMap(),
    @SerializedName("ollama_reachable") val ollamaReachable: Boolean = false
)

data class MemoryTtlSettings(
    @SerializedName("summaries_enabled") val summariesEnabled: Boolean = true,
    @SerializedName("default_ttl") val ttl: String = "standard",
    @SerializedName("auto_extend") val autoExtend: Boolean = true,
    @SerializedName("ttl_options") val ttlOptions: List<String> = emptyList()
)

data class VoiceOption(
    @SerializedName("voice_id") val id: String,
    @SerializedName("display_name") val name: String,
    val provider: String = "piper",
    val engine: String = "piper",
    val accent: String = "american",
    val gender: String = "female",
    val installed: Boolean = true,
    val active: Boolean = false,
    @SerializedName("size_mb") val sizeMb: Int? = null,
    val description: String? = null
)

data class N8nSettings(
    val enabled: Boolean = false,
    val url: String = "",
    @SerializedName("api_key") val apiKey: String = "",
    @SerializedName("webhook_secret") val webhookSecret: String = ""
)
