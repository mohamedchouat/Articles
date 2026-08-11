package com.chtmed.articles

import android.app.Application
import com.chtmed.restapidebugger.RestApiDebugger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ArticlesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RestApiDebugger.initialize(this, BuildConfig.DEBUG_REST_API)
    }
}
