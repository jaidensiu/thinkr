package com.example.thinkr.app

import android.app.Application
import com.example.thinkr.di.KoinInitializer.initKoin
import org.koin.android.ext.koin.androidContext

class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@Application)
        }
    }
}
