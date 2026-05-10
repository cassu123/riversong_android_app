package com.riversongai.data.model

import com.google.gson.annotations.SerializedName

data class Vehicle(
    val id: String = "",
    val year: Int = 0,
    val make: String = "",
    val model: String = "",
    val trim: String = "",
    val nickname: String = "",
    @SerializedName("vehicle_type") val vehicleType: String = "auto",
    val vin: String = "",
    @SerializedName("license_plate") val licensePlate: String = "",
    val color: String = "",
    val notes: String = "",
    @SerializedName("fluid_specs") val fluidSpecs: List<FluidSpec> = emptyList(),
    @SerializedName("torque_specs") val torqueSpecs: List<TorqueSpec> = emptyList(),
    @SerializedName("check_points") val checkPoints: List<ServiceCheckpoint> = emptyList()
)

data class FluidSpec(
    val id: String,
    val name: String,
    val spec: String,
    val volume: String
)

data class TorqueSpec(
    val id: String,
    val name: String,
    @SerializedName("ft_lb") val ftLb: Float?,
    val nm: Float?
)

data class ServiceCheckpoint(
    val id: String = "",
    val description: String = "",
    @SerializedName("sort_order") val sortOrder: Int = 0,
    @SerializedName("service_level") val serviceLevel: String = "inspect",
    @SerializedName("interval_miles") val intervalMiles: Int? = null,
    @SerializedName("interval_days") val intervalDays: Int? = null,
    @SerializedName("due_at_miles") val dueAtMiles: Int? = null,
    @SerializedName("expected_spec") val expectedSpec: String = "",
    val unit: String = "",
    val volume: String = "",
    @SerializedName("last_service_odometer") val lastServiceOdometer: Int? = null,
    @SerializedName("last_service_date") val lastServiceDate: String? = null
)

data class ServiceLog(
    val id: String = "",
    @SerializedName("vehicle_id") val vehicleId: String = "",
    @SerializedName("service_date") val serviceDate: String = "",
    @SerializedName("service_type") val serviceType: String = "",
    val odometer: Int? = null,
    @SerializedName("is_pro_service") val isProService: Boolean = false,
    @SerializedName("service_center") val serviceCenter: String? = null,
    val cost: Double? = null,
    val notes: String = "",
    @SerializedName("receipt_path") val receiptPath: String? = null,
    @SerializedName("performed_by") val performedBy: Map<String, String>? = null,
    @SerializedName("check_results") val checkResults: List<CheckResult> = emptyList()
)

data class CheckResult(
    val id: String,
    @SerializedName("check_point_id") val checkPointId: String?,
    val description: String,
    @SerializedName("actual_value") val actualValue: String?,
    val status: String,
    val passed: Boolean
)

data class VehicleAssignment(
    val id: String,
    @SerializedName("vehicle_id") val vehicleId: String,
    @SerializedName("person_id") val personId: String,
    @SerializedName("person_email") val personEmail: String?,
    @SerializedName("person_display_name") val personDisplayName: String?
)

data class CreateVehicle(
    val make: String,
    val model: String,
    val year: Int? = null,
    val trim: String = "",
    val nickname: String = "",
    @SerializedName("vehicle_type") val vehicleType: String = "auto",
    val vin: String = "",
    @SerializedName("license_plate") val licensePlate: String = "",
    val color: String = "",
    val notes: String = ""
)

data class CreateServiceLog(
    @SerializedName("service_date") val serviceDate: String,
    val odometer: Int? = null,
    @SerializedName("is_pro_service") val isProService: Boolean = false,
    @SerializedName("service_center") val serviceCenter: String? = null,
    @SerializedName("service_type") val serviceType: String? = null,
    val cost: Double? = null,
    val notes: String = ""
)
