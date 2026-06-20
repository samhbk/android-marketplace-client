package com.marketplace.app

import android.app.Application
import com.marketplace.app.di.AppContainer

class MarketplaceApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
