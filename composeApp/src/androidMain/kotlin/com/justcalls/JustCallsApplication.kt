package com.justcalls

import android.app.Application

class JustCallsApplication : Application() {
    companion object {
        lateinit var instance: JustCallsApplication
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}

