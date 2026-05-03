package com.riversongai.data.remote

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.riversongai.BuildConfig
import com.riversongai.data.model.AuthResponse
import com.riversongai.data.model.Device
import com.riversongai.data.model.DashboardStats
import com.riversongai.data.model.Fact
import com.riversongai.data.model.FactCreate
import com.riversongai.data.model.FeedPreferences
import com.riversongai.data.model.LlmSettings
import com.riversongai.data.model.ModelCatalog
import com.riversongai.data.model.NewsArticle
import com.riversongai.data.model.Routine
import com.riversongai.data.model.RoutineCreate
import com.riversongai.data.model.RoutineRunResponse
import com.riversongai.data.model.StockQuote
import com.riversongai.data.model.User
import com.riversongai.data.model.WeatherData
import com.riversongai.utils.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
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

    // Memory
    @GET("api/memory/facts")
    suspend fun getFacts(): Response<List<Fact>>

    @POST("api/memory/facts")
    suspend fun createFact(@Body request: FactCreate): Response<Fact>

    @DELETE("api/memory/facts/{id}")
    suspend fun deleteFact(@Path("id") factId: String): Response<Void>

    // Routines
    @GET("api/routines")
    suspend fun getRoutines(): Response<List<Routine>>

    @POST("api/routines")
    suspend fun createRoutine(@Body request: RoutineCreate): Response<Routine>

    @PATCH("api/routines/{id}")
    suspend fun updateRoutine(@Path("id") routineId: String, @Body fields: Map<String, @JvmSuppressWildcards Any?>): Response<Routine>

    @DELETE("api/routines/{id}")
    suspend fun deleteRoutine(@Path("id") routineId: String): Response<Void>

    @POST("api/routines/{id}/run")
    suspend fun runRoutine(@Path("id") routineId: String): Response<RoutineRunResponse>

    // Feeds
    @GET("api/feeds/news")
    suspend fun getNews(): Response<List<NewsArticle>>

    @GET("api/feeds/weather")
    suspend fun getWeather(): Response<WeatherData>

    @GET("api/feeds/stocks")
    suspend fun getStocks(): Response<List<StockQuote>>

    @GET("api/feeds/preferences")
    suspend fun getFeedPreferences(): Response<FeedPreferences>

    @PUT("api/feeds/preferences")
    suspend fun saveFeedPreferences(@Body prefs: FeedPreferences): Response<Void>

    // Settings
    @GET("api/models")
    suspend fun getModels(): Response<ModelCatalog>

    @GET("api/settings/llm")
    suspend fun getLlmSettings(): Response<LlmSettings>

    @POST("api/settings/llm")
    suspend fun saveLlmSettings(@Body body: Map<String, @JvmSuppressWildcards Any?>): Response<Void>

    // Dashboard
    @GET("api/dashboard")
    suspend fun getDashboard(): Response<DashboardStats>

    // Transcribe (voice)
    @POST("api/conversation/transcribe")
    suspend fun transcribeAudio(@Body request: TranscribeRequest): Response<TranscribeResponse>

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

data class TranscribeRequest(val audio: String)

data class TranscribeResponse(val text: String)
