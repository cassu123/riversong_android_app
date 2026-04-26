package com.riversongai.data.model

data class SmartHomeSummary(
    val totalDevices: Int,
    val activeDevices: Int,
    val offlineDevices: Int
)

data class ActivitySummary(
    val stepsTaken: Int = 0,
    val activeMinutes: Int = 0,
    val summary: String = "No activity data yet."
)
