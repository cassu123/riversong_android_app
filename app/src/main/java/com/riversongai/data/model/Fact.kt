package com.riversongai.data.model

import com.google.gson.annotations.SerializedName

data class Fact(
    val id: String,
    val key: String,
    val value: String,
    val source: String = "manual",
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class FactCreate(val key: String, val value: String)
