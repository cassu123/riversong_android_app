package com.riversongai.data.model

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    val message: String,
    val history: List<Map<String, String>> = emptyList(),
    val provider: String? = null,
    val model: String? = null
)

data class ChatResponse(
    val response: String = "",
    val model: String = "",
    @SerializedName("session_id") val sessionId: String? = null
)

data class TranscribeRequest(
    val audio: String
)

data class TranscribeResponse(
    val text: String
)
