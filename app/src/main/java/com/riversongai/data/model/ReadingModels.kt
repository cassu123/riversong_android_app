package com.riversongai.data.model

data class Book(
    val id: String,
    val service: String,
    val title: String,
    val author: String,
    val coverUrl: String?,
    val progressPct: Int,
    val status: String, // "reading"|"finished"|"want_to_read"|"dnf"
    val rating: Int?,
    val notes: String?,
    val launchUrl: String?
)

data class BookCreate(
    val service: String,
    val title: String,
    val author: String,
    val coverUrl: String? = null,
    val progressPct: Int = 0,
    val status: String = "want_to_read",
    val rating: Int? = null,
    val notes: String? = null,
    val launchUrl: String? = null
)

data class BookUpdate(
    val service: String? = null,
    val title: String? = null,
    val author: String? = null,
    val coverUrl: String? = null,
    val progressPct: Int? = null,
    val status: String? = null,
    val rating: Int? = null,
    val notes: String? = null,
    val launchUrl: String? = null
)

data class ReadingStats(
    val total: Int,
    val byStatus: Map<String, Int>,
    val byService: Map<String, Int>
)

data class ReadingConnections(
    val libby: Boolean,
    val audible: Boolean,
    val kindle: Boolean,
    val googlePlay: Boolean
)

data class LibbyLoan(
    val title: String,
    val author: String,
    val formatId: String,
    val expires: String,
    val daysRemaining: Int,
    val percentComplete: Int,
    val coverUrl: String?
)

data class LibbyHold(
    val title: String,
    val author: String,
    val formatId: String,
    val queuePosition: Int,
    val queueSize: Int,
    val estimatedWaitDays: Int,
    val coverUrl: String?
)
