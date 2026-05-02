package com.riversongai.data.remote

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.riversongai.data.model.AuthResponse
import com.riversongai.data.model.Device
import com.riversongai.data.model.User
import com.riversongai.BuildConfig
import com.riversongai.utils.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface RiverSongApiService {

    @POST("api/v1/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/v1/auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("api/v1/users/me")
    suspend fun getCurrentUser(): Response<User>

    @GET("api/v1/devices")
    suspend fun getAllDevices(): Response<List<Device>>

    @GET("api/v1/devices/{deviceId}")
    suspend fun getDeviceById(@Path("deviceId") deviceId: String): Response<Device>

    @PUT("api/v1/devices/{deviceId}/control")
    suspend fun controlDevice(
        @Path("deviceId") deviceId: String,
        @Body controlRequest: DeviceControlRequest
    ): Response<Device>

    @POST("api/v1/ai/audio/process")
    suspend fun processAudio(@Body audioData: AudioProcessRequest): Response<AudioProcessResponse>

    @POST("api/v1/ai/image/analyze")
    suspend fun analyzeImage(@Body imageData: ImageAnalyzeRequest): Response<ImageAnalyzeResponse>

    companion object {
        fun create(baseUrl: String, sessionManager: SessionManager): RiverSongApiService {
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(sessionManager))
                .apply {
                    if (BuildConfig.DEBUG) {
                        addInterceptor(HttpLoggingInterceptor().apply {
                            // BASIC only — never log bodies which may contain passwords
                            level = HttpLoggingInterceptor.Level.BASIC
                        })
                    }
                }
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()

            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(RiverSongApiService::class.java)
        }
    }
}

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val role: String = "PARENT"
)

data class DeviceControlRequest(
    val command: String,
    val value: String? = null
)

data class AudioProcessRequest(
    val audioBase64: String,
    val format: String = "wav",
    val type: String = "voice_command"
)

data class AudioProcessResponse(
    val success: Boolean,
    val message: String,
    val recognizedText: String? = null,
    val classification: String? = null
)

data class ImageAnalyzeRequest(
    val imageBase64: String,
    val analysisType: String = "object_detection"
)

data class ImageAnalyzeResponse(
    val success: Boolean,
    val message: String,
    val analysisResult: Map<String, String>? = null
)
