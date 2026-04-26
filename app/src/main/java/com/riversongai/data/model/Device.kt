package com.riversongai.data.model

data class Device(
    val id: String,
    val name: String,
    val type: String,
    val status: String,
    val location: String,
    val isOn: Boolean? = null,
    val temperature: Float? = null,
    val brightness: Int? = null,
    val batteryLevel: Int? = null,
    val streamUrl: String? = null,
    val lastUpdated: Long? = null
)
