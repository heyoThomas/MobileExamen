package com.example.mobilemonteurpvbthomasanjema

import android.app.Application

class MonteurApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MeldingRepository.init(this)
    }
}
