// app/src/main/java/com/riversongai/data/remote/RiverSongApiService.kt
//
// This file defines the API service interface for the River Song Android application.
// It acts as the bridge for communication between the Android frontend and the
// Python backend API, specifying the network requests (endpoints) that the app can make
// to interact with the AI system for user management, device control, and data retrieval.

package com.riversongai.data.remote

import com.riversongai.data.model.Device
import com.riversongai.data.model.User
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Section: API Service Interface
// This interface defines the contract for API communication using Retrofit.
// Each function corresponds to an API endpoint on your Python backend.
interface RiverSongApiService {

    // Section: User Authentication Endpoints

    // Authenticates a user.
    // @POST: Indicates a POST request.
    // @Body: Sends the User object (or a dedicated LoginRequest object) in the request body.
    // Response<User>: Expected response is a User object wrapped in Retrofit's Response.
    @POST("api/v1/auth/login")
    suspend fun loginUser(@Body user: User): Response<User> // Or a specific LoginRequest data class

    // Registers a new user.
    @POST("api/v1/auth/register")
    suspend fun registerUser(@Body user: User): Response<User> // Or a specific RegisterRequest data class

    // Retrieves the currently authenticated user's profile.
    // @GET: Indicates a GET request.
    // @Header: Sends an Authorization token (e.g., Bearer Token) for authenticated requests.
    @GET("api/v1/users/me")
    suspend fun getCurrentUser(@Header("Authorization") authToken: String): Response<User>

    // Section: Device Management Endpoints

    // Retrieves a list of all smart home devices associated with the user.
    @GET("api/v1/devices")
    suspend fun getAllDevices(@Header("Authorization") authToken: String): Response<List<Device>>

    // Retrieves a specific device by its ID.
    // @Path: Injects the device ID into the URL path.
    @GET("api/v1/devices/{deviceId}")
    suspend fun getDeviceById(
        @Header("Authorization") authToken: String,
        @Path("deviceId") deviceId: String
    ): Response<Device>

    // Sends a command to control a specific device (e.g., turn on/off light).
    // @PUT: Indicates a PUT request for updating a resource.
    // @Body: Sends the command details (e.g., a DeviceControlRequest data class).
    @PUT("api/v1/devices/{deviceId}/control")
    suspend fun controlDevice(
        @Header("Authorization") authToken: String,
        @Path("deviceId") deviceId: String,
        @Body controlRequest: DeviceControlRequest // A data class like: data class DeviceControlRequest(val command: String, val value: Any?)
    ): Response<Device> // Or a specific ControlResponse data class

    // Section: AI-Specific Endpoints (Examples)

    // Submits audio data for speech recognition or analysis.
    // This might require a specific MultipartBody.Part for file uploads.
    @POST("api/v1/ai/audio/process")
    suspend fun processAudio(@Header("Authorization") authToken: String, @Body audioData: AudioProcessRequest): Response<AudioProcessResponse>

    // Submits image data for analysis (e.g., facial recognition, object detection).
    @POST("api/v1/ai/image/analyze")
    suspend fun analyzeImage(@Header("Authorization") authToken: String, @Body imageData: ImageAnalyzeRequest): Response<ImageAnalyzeResponse>


    // Section: Companion Object for Service Creation
    // A companion object provides a static-like method to create and configure
    // the Retrofit API service instance, typically called once during app initialization.
    companion object {
        private const val BASE_URL = "http://localhost:5000/" // Default placeholder URL. Will be overridden by App.kt

        // Function to create and return a configured instance of RiverSongApiService.
        fun create(baseUrl: String): RiverSongApiService {
            // Section: Retrofit Builder
            // Configures Retrofit with the base URL and a converter factory.
            // GsonConverterFactory is commonly used for JSON serialization/deserialization.
            // Ensure you have the 'com.squareup.retrofit2:converter-gson' dependency.
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl) // The base URL of your Python backend API.
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            // Creates an implementation of the API interface.
            return retrofit.create(RiverSongApiService::class.java)
        }
    }
}

// Section: Placeholder Data Classes for Requests/Responses (You'll define these in data.model or data.remote)
// These are examples of simple data classes you might need for API requests and responses.
// You'll define their actual structures based on your Python backend's API contracts.

data class LoginRequest(
    val username: String,
    val password_hash: String // Assuming password hash is sent, or a plain password for backend hashing
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password_hash: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val role: String // e.g., "CHILD", "PARENT"
)

data class DeviceControlRequest(
    val command: String, // e.g., "turn_on", "turn_off", "set_temperature", "set_brightness"
    val value: Any? = null // e.g., true, false, 22.5, 75
)

data class AudioProcessRequest(
    val audioBase64: String, // Base64 encoded audio data
    val format: String = "wav",
    val type: String = "voice_command" // e.g., "voice_command", "sound_classification"
)

data class AudioProcessResponse(
    val success: Boolean,
    val message: String,
    val recognizedText: String? = null,
    val classification: String? = null
)

data class ImageAnalyzeRequest(
    val imageBase64: String, // Base64 encoded image data
    val analysisType: String = "object_detection" // e.g., "facial_recognition", "activity_recognition"
)

data class ImageAnalyzeResponse(
    val success: Boolean,
    val message: String,
    val analysisResult: Map<String, Any?>? = null // Flexible map for various analysis results
)