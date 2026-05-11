package com.riversongai.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Routine(
    val id: String,
    val name: String,
    @SerializedName("trigger_type") val trigger: String = "manual",
    @SerializedName("schedule_time") val time: String? = null,
    @SerializedName("schedule_days") val days: List<String> = emptyList(),
    val prompt: String = "",
    @SerializedName("is_enabled") val isEnabled: Boolean = true,
    @SerializedName("last_run_at") val lastRun: String? = null
) : Serializable

data class RoutineCreate(
    val name: String,
    @SerializedName("trigger_type") val trigger: String = "manual",
    @SerializedName("schedule_time") val time: String? = null,
    @SerializedName("schedule_days") val days: List<String> = emptyList(),
    val prompt: String = "",
    @SerializedName("is_enabled") val isEnabled: Boolean = true
)

data class RoutineRunResponse(val output: String)
