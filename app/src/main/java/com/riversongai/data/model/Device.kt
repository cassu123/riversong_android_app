// app/src/main/java/com/riversongai/data/model/Device.kt
//
// This file defines the data model for a smart home device.
// It serves as a blueprint for representing individual devices managed or
// monitored by the River Song AI system within the Android application's data layer.

package com.riversongai.data.model

// Section: Data Class Definition
// A data class is a concise way to create classes that solely hold data.
// It automatically provides useful functions like equals(), hashCode(), toString(),
// copy(), and componentN() methods, which are ideal for model classes.
data class Device(
    // Section: Device Identifier
    // Unique identifier for the device. This should typically match the ID used in the backend.
    val id: String,

    // Section: Basic Device Information
    // Human-readable name of the device.
    val name: String,
    // Type of the device (e.g., "light", "thermostat", "speaker", "camera", "sensor").
    val type: String,
    // Current operational status of the device (e.g., "online", "offline", "on", "off", "active").
    val status: String,
    // The physical location of the device within the home (e.g., "Living Room", "Bedroom 1").
    val location: String,

    // Section: Device Specific Properties (Optional and extensible)
    // These properties are optional and can be null or default values if not applicable
    // to a specific device type. They can be expanded as needed.

    // Indicates if a controllable device is currently on or off.
    val isOn: Boolean? = null,
    // Current temperature if the device is a thermostat or temperature sensor.
    val temperature: Float? = null,
    // Brightness level for lights (e.g., 0-100).
    val brightness: Int? = null,
    // Battery level for battery-powered devices.
    val batteryLevel: Int? = null,
    // URL for streaming if the device is a camera.
    val streamUrl: String? = null,

    // Section: Metadata
    // Timestamp of the last known update for the device's state.
    val lastUpdated: Long? = null // Using Long for Unix timestamp in milliseconds
)