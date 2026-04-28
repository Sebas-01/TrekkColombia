package com.trekking.app

import android.app.Application

class TrekkingApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: TrekkingApplication
            private set
    }
}
