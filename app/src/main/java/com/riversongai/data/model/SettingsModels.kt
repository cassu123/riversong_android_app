package com.riversongai.data.model

data class VoiceOption(
    val id: String,
    val name: String,
    val provider: String
)

data class MemoryTtlSettings(
    val ttl: String,
    val autoExtend: Boolean
)
