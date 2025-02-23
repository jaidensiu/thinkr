package com.example.thinkr.di

import com.example.thinkr.ui.landing.LandingScreenViewModel
import com.example.thinkr.ui.home.HomeScreenViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::LandingScreenViewModel)
    viewModelOf(::HomeScreenViewModel)
}
