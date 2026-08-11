package com.chtmed.restapidebugger.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat

internal object NotificationHelper {
    const val CHANNEL_ID = "rest_api_debugger"
    const val NOTIFICATION_ID = 84_251_001

    @Volatile
    private var channelCreated = false

    fun ensureChannel(context: Context) {
        if (channelCreated || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            channelCreated = true
            return
        }
        val channel = NotificationChannel(
            CHANNEL_ID,
            "REST API Debugger",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Live log of REST API calls made by the app (debug builds only)."
            setShowBadge(false)
        }
        NotificationManagerCompat.from(context).createNotificationChannel(channel)
        channelCreated = true
    }
}
