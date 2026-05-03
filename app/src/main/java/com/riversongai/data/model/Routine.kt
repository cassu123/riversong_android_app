package com.riversongai.data.model

import com.google.gson.annotations.SerializedName

data class Routine(
    val id: String,
    val name: String,
    @SerializedName("trigger_type") val triggerType: String = "manual",
    @SerializedName("schedule_time") val scheduleTime: String? = null,
    @SerializedName("schedule_days") val scheduleDays: List<String>? = emptyList(),
    val prompt: String = "",
    @SerializedName("is_enabled") val isEnabled: Boolean = true,
    @SerializedName("last_run_at") val lastRunAt: Long? = null
)

data class RoutineCreate(
    val name: String,
    @SerializedName("trigger_type") val triggerType: String = "manual",
    @SerializedName("schedule_time") val scheduleTime: String? = null,
    @SerializedName("schedule_days") val scheduleDays: List<String>? = emptyList(),
    val prompt: String = "",
    @SerializedName("is_enabled") val isEnabled: Boolean = true
)

data class RoutineRunResponse(val output: String)
