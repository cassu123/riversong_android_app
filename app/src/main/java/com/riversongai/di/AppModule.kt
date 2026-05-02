package com.riversongai.di

import com.riversongai.data.remote.RiverSongApiService
import com.riversongai.data.repository.ConversationRepository
import com.riversongai.data.repository.SmartHomeRepository
import com.riversongai.data.repository.UserRepository
import com.riversongai.ui.viewmodel.ChatViewModel
import com.riversongai.ui.viewmodel.HomeViewModel
import com.riversongai.ui.viewmodel.LoginViewModel
import com.riversongai.ui.viewmodel.RegisterViewModel
import com.riversongai.ui.viewmodel.SmartHomeControlViewModel
import com.riversongai.ui.viewmodel.UserDashboardViewModel
import com.riversongai.utils.Constants
import com.riversongai.utils.SessionManager
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single { SessionManager(androidContext()) }
    single { RiverSongApiService.create(Constants.BASE_URL, get()) }

    single { UserRepository(get()) }
    single { SmartHomeRepository(get()) }
    single { ConversationRepository(get()) }

    viewModel { LoginViewModel(get(), get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { UserDashboardViewModel(get(), get(), get()) }
    viewModel { SmartHomeControlViewModel(get(), get()) }
    viewModel { ChatViewModel(get()) }
}
