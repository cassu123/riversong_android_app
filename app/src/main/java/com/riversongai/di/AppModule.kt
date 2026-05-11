package com.riversongai.di

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.riversongai.data.remote.RiverSongApiService
import com.riversongai.data.repository.*
import com.riversongai.ui.viewmodel.*
import com.riversongai.utils.Constants
import com.riversongai.utils.SessionManager
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {

    single { SessionManager(androidContext()) }
    
    single {
        val sessionManager: SessionManager = get()
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                sessionManager.getAuthToken()?.let {
                    requestBuilder.header("Authorization", "Bearer $it")
                }
                chain.proceed(requestBuilder.build())
            }
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        val gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(RiverSongApiService::class.java)
    }

    single { UserRepository(get()) }
    single { SmartHomeRepository(get()) }
    single { ConversationRepository(get()) }
    single { MemoryRepository(get()) }
    single { FeedsRepository(get()) }
    single { RoutinesRepository(get()) }
    single { SettingsRepository(get()) }
    single { SportsRepository(get()) }
    single { InventoryRepository(get()) }
    single { MaintenanceRepository(get()) }
    single { CommerceRepository(get()) }

    viewModel { LoginViewModel(get(), get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { HomeViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { UserDashboardViewModel(get(), get()) }
    viewModel { SmartHomeControlViewModel(get(), get()) }
    viewModel { ChatViewModel(get(), get(), get()) }
    viewModel { MemoryViewModel(get()) }
    viewModel { FeedsViewModel(get()) }
    viewModel { SportsViewModel(get()) }
    viewModel { RoutinesViewModel(get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { InventoryViewModel(get()) }
    viewModel { MaintenanceViewModel(get()) }
    viewModel { CommerceViewModel(get()) }
    viewModel { AnalyticsViewModel(get()) }
    viewModel { CulinaryViewModel(get()) }
    viewModel { ReadingViewModel(get()) }
    viewModel { GoogleViewModel(get()) }
    viewModel { UsersViewModel(get()) }
}
