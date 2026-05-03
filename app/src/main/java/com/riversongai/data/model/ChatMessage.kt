package com.riversongai.data.model

data class ChatMessage(
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    val isUser: Boolean get() = role == "user"
    val isStreaming: Boolean get() = role == "assistant_streaming"
}

data class ChatModel(
    val id: String,
    val name: String,
    val provider: String,
    val isLocal: Boolean
)

data class ChatSession(
    val id: String,
    val title: String,
    val timestamp: Long,
    val messageCount: Int,
    val model: String
)

data class ChatSessionDetail(
    val id: String,
    val messages: List<ChatMessage>
)
