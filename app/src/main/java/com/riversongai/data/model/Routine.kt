package com.riversongai.data.model

import com.google.gson.annotations.SerializedName

data class Routine(
    val id: String,
    val name: String,
    val trigger: String = "manual",
    val time: String? = null,
    val days: List<String> = emptyList(),
    val prompt: String = "",
    val enabled: Boolean = true,
    @SerializedName("last_run") val lastRun: String? = null
)

data class RoutineCreate(
    val name: String,
    val trigger: String = "manual",
    val time: String? = null,
    val days: List<String> = emptyList(),
    val prompt: String = "",
    val enabled: Boolean = true
)

data class RoutineRunResponse(val output: String)
