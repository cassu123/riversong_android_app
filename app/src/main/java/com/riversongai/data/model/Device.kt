package com.riversongai.data.model

import com.google.gson.annotations.SerializedName

data class Device(
    @SerializedName("entity_id") val entityId: String,
    val state: String = "unknown",
    val attributes: Map<String, Any?> = emptyMap()
) {
    val domain: String get() = entityId.split(".").first()

    val name: String get() = (attributes["friendly_name"] as? String)
        ?: entityId.split(".").drop(1).joinToString(" ")
            .replace("_", " ")
            .replaceFirstChar { it.uppercase() }

    val isOn: Boolean get() = state in setOf("on", "playing", "home", "open", "unlocked", "active")

    val brightnessPercent: Int? get() = (attributes["brightness"] as? Number)
        ?.toInt()?.let { (it / 255.0 * 100).toInt() }

    val temperature: Float? get() = (attributes["temperature"] as? Number)?.toFloat()

    val currentTemperature: Float? get() = (attributes["current_temperature"] as? Number)?.toFloat()

    val icon: String get() = when (domain) {
        "light" -> "💡"
        "switch" -> "🔌"
        "fan" -> "🌀"
        "cover" -> "🪟"
        "lock" -> "🔒"
        "climate" -> "🌡️"
        "scene" -> "🎭"
        "script" -> "⚡"
        "input_boolean" -> "🔘"
        else -> "📱"
    }
}
