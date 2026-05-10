package com.riversongai.data.model

data class GoogleStatus(
    val connected: Boolean,
    val email: String?,
    val expired: Boolean?
)

data class GoogleAuthUrl(
    val authUrl: String
)

data class CalendarEvent(
    val id: String,
    val summary: String,
    val start: String, // ISO datetime
    val location: String?
)

data class CalendarResponse(
    val events: List<CalendarEvent>
)

data class GmailMessage(
    val id: String,
    val from: String,
    val subject: String
)

data class GmailResponse(
    val messages: List<GmailMessage>
)
