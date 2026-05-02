package com.riversongai.data.remote

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.riversongai.BuildConfig
import com.riversongai.data.model.AuthResponse
import com.riversongai.data.model.Device
import com.riversongai.data.model.User
import com.riversongai.utils.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface RiverSongApiService {

    @POST("api/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/signup")
    suspend fun signupUser(@Body request: SignupRequest): Response<Void>

    @GET("api/auth/me")
    suspend fun getCurrentUser(): Response<User>

    @GET("api/home/devices")
    suspend fun getDevices(): Response<List<Device>>

    @POST("api/home/action")
    suspend fun callAction(@Body request: HomeActionRequest): Response<HomeActionResponse>

    @POST("api/conversation/chat")
    suspend fun chatHttp(@Body request: ChatRequest): Response<okhttp3.ResponseBody>

    companion object {
        fun create(baseUrl: String, sessionManager: SessionManager): RiverSongApiService {
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(sessionManager))
                .apply {
                    if (BuildConfig.DEBUG) {
                        addInterceptor(HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BASIC
                        })
                    }
                }
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
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
    val email: String,
    val password: String
)

data class SignupRequest(
    val email: String,
    val password: String,
    @com.google.gson.annotations.SerializedName("display_name") val displayName: String
)

data class HomeActionRequest(
    @com.google.gson.annotations.SerializedName("entity_id") val entityId: String,
    val action: String,
    @com.google.gson.annotations.SerializedName("brightness_pct") val brightnessPct: Int? = null,
    val temperature: Float? = null
)

data class HomeActionResponse(
    val ok: Boolean,
    val detail: String? = null
)

data class ChatRequest(
    val message: String,
    val history: List<Map<String, String>> = emptyList()
)
