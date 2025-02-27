package com.example.thinkr.di

import com.example.thinkr.data.remote.HttpClientFactory
import com.example.thinkr.data.remote.RemoteApi
import com.example.thinkr.data.remote.RemoteApiImpl
import com.example.thinkr.data.repositories.AuthRepository
import com.example.thinkr.data.repositories.AuthRepositoryImpl
import com.example.thinkr.ui.home.HomeScreenViewModel
import com.example.thinkr.ui.landing.LandingScreenViewModel
import com.example.thinkr.ui.payment.PaymentViewModel
import com.example.thinkr.ui.profile.ProfileViewModel
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single<HttpClientEngine> { OkHttp.create() }
    single { HttpClientFactory.create(get()) }
    singleOf(::RemoteApiImpl).bind<RemoteApi>()
    singleOf(::AuthRepositoryImpl).bind<AuthRepository>()
    viewModelOf(::LandingScreenViewModel)
    viewModelOf(::HomeScreenViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::PaymentViewModel)
}
