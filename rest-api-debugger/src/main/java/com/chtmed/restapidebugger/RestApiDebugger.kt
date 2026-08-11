package com.chtmed.restapidebugger

import android.content.Context
import android.content.Intent
import com.chtmed.restapidebugger.interceptor.ApiCallInterceptor
import com.chtmed.restapidebugger.notification.NotificationHelper
import com.chtmed.restapidebugger.store.ApiCallHistoryStore
import com.chtmed.restapidebugger.ui.RestApiDebuggerActivity
import okhttp3.Interceptor

/**
 * Public entry point for the REST API debugger module.
 *
 * Integration is two calls:
 * ```
 * // Application.onCreate()
 * RestApiDebugger.initialize(this, BuildConfig.DEBUG_REST_API)
 *
 * // wherever the app's OkHttpClient.Builder is configured
 * okHttpClientBuilder.addInterceptor(RestApiDebugger.interceptor())
 * ```
 * When [initialize] is called with `enabled = false` (or never called at
 * all), [interceptor] still returns a valid `Interceptor`, but it does
 * nothing beyond an immediate `chain.proceed()` — no capturing, masking,
 * storage, or notifications happen, and no existing Retrofit/OkHttp
 * interfaces need to change.
 */
object RestApiDebugger {

    @Volatile
    internal var isEnabled: Boolean = false
        private set

    @Volatile
    private var applicationContext: Context? = null

    fun initialize(context: Context, enabled: Boolean) {
        isEnabled = enabled
        if (enabled) {
            val appContext = context.applicationContext
            applicationContext = appContext
            NotificationHelper.ensureChannel(appContext)
        } else {
            applicationContext = null
            ApiCallHistoryStore.clear()
        }
    }

    /** Add this to an OkHttpClient.Builder via `.addInterceptor(...)`. Safe to keep wired even when disabled. */
    fun interceptor(): Interceptor = ApiCallInterceptor()

    /** Intent that opens the debugger's history screen; used by the notification and can be launched directly too. */
    fun historyScreenIntent(context: Context): Intent = RestApiDebuggerActivity.newIntent(context)

    fun clearHistory() = ApiCallHistoryStore.clear()

    internal fun context(): Context? = applicationContext
}
