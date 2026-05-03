package com.riversongai.data.model

data class ChatMessage(
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    val isUser: Boolean get() = role == "user"
    val isStreaming: Boolean get() = role == "assistant_streaming"
}
