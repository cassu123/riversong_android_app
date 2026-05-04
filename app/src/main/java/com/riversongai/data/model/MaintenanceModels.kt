package com.riversongai.data.model

data class Vehicle(
    val id: String = "",
    val make: String = "",
    val model: String = "",
    val year: Int = 0,
    val trim: String = "",
    val nickname: String = "",
    val vehicleType: String = "auto",
    val color: String = "",
    val vin: String = "",
)

data class ServiceCheckpoint(
    val id: String = "",
    val description: String = "",
    val serviceLevel: String = "SERVICE",
    val intervalMiles: Int? = null,
    val intervalDays: Int? = null,
    val dueAtMiles: Int? = null,
    val lastServiceOdometer: Int? = null,
    val expectedSpec: String = "",
    val unit: String = "",
    val volume: String = "",
)

data class ServiceLog(
    val id: String = "",
    val serviceDate: String = "",
    val odometer: Int? = null,
    val serviceType: String = "",
    val isProService: Boolean = false,
    val serviceCenterName: String = "",
    val cost: Double? = null,
    val performedById: String? = null,
)

data class CreateVehicle(
    val make: String,
    val model: String,
    val year: Int,
    val trim: String = "",
    val nickname: String = "",
    val vehicleType: String = "auto",
    val color: String = "",
    val vin: String = "",
)

data class CreateServiceLog(
    val serviceDate: String,
    val odometer: Int?,
    val serviceType: String = "General Service",
    val isProService: Boolean = false,
    val serviceCenterName: String = "",
    val cost: Double? = null,
)
