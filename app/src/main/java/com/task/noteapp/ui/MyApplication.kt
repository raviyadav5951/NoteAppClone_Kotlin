package com.task.noteapp.ui

import android.app.Application
import com.task.noteapp.BuildConfig
import timber.log.Timber

class MyApplication:Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}