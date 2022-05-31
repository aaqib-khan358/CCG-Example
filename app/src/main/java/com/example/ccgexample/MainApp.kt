package com.example.ccgexample

import android.app.Application
import android.util.Log
import com.google.android.gms.ads.MobileAds

class MainApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) {
            it.adapterStatusMap.forEach { entry ->
                Log.d("CCGApp", "ADAPTER-> ${entry.key} : ${entry.value.description}, ${entry.value.initializationState}, ${entry.value.latency}")
            }
        }
    }
}