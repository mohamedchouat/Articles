package com.chtmed.restapidebugger.notification

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.chtmed.restapidebugger.RestApiDebugger
import com.chtmed.restapidebugger.model.ApiCallRecord
import com.chtmed.restapidebugger.store.ApiCallHistoryStore
import com.chtmed.restapidebugger.util.DebuggerColors
import com.chtmed.restapidebugger.util.JsonFormatter

/**
 * Maintains one ongoing, updating notification (rather than one notification
 * per call) showing the most recent API activity. Tapping it opens the full
 * history screen.
 */
internal object ApiDebuggerNotifier {
    private const val MAX_CALLS_SHOWN = 5
    private const val BODY_MAX_LENGTH = 220

    fun onNewCall(record: ApiCallRecord) {
        val context = RestApiDebugger.context() ?: return
        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) return

        val recent = ApiCallHistoryStore.history.value.take(MAX_CALLS_SHOWN)
        val bigText = buildSummary(recent)

        val contentIntent = androidx.core.app.TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(RestApiDebugger.historyScreenIntent(context))
            .getPendingIntent(
                0,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("REST API Debugger")
            .setContentText(summaryLine(record))
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setContentIntent(contentIntent)
            .setOngoing(false)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        try {
            notificationManager.notify(NotificationHelper.NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS not granted at runtime on API 33+ — fail silently,
            // the history screen is still reachable directly from the host app.
        }
    }

    private fun summaryLine(record: ApiCallRecord): String {
        val status = record.statusCode?.let { "$it" } ?: "ERR"
        return "${record.method} ${record.path} • $status • ${record.durationMs ?: 0} ms"
    }

    private fun buildSummary(records: List<ApiCallRecord>): SpannableStringBuilder {
        val builder = SpannableStringBuilder()
        records.forEachIndexed { index, record ->
            if (index > 0) builder.append("\n\n")
            appendColored(builder, "${record.method} ${record.url}", DebuggerColors.forMethod(record.method), bold = true)
            builder.append("\n")

            val statusText = record.statusCode?.let { "$it ${record.statusMessage.orEmpty()}".trim() }
                ?: record.errorMessage?.let { "ERROR: $it" }
                ?: "ERROR"
            appendColored(builder, statusText, DebuggerColors.forStatus(record.statusCode))
            appendColored(builder, " • ${record.durationMs ?: 0} ms", DebuggerColors.DURATION)

            appendBodyPreview(builder, record)
        }
        applyMonospace(builder)
        return builder
    }

    private fun appendBodyPreview(builder: SpannableStringBuilder, record: ApiCallRecord) {
        when {
            record.isRequestBearingMethod -> {
                record.requestBody?.takeIf { it.isNotBlank() }?.let {
                    builder.append("\nRequest:\n").append(JsonFormatter.compact(it, BODY_MAX_LENGTH))
                }
                record.responseBody?.takeIf { it.isNotBlank() }?.let {
                    builder.append("\nResponse:\n").append(JsonFormatter.compact(it, BODY_MAX_LENGTH))
                }
            }
            else -> {
                record.responseBody?.takeIf { it.isNotBlank() }?.let {
                    builder.append("\n").append(JsonFormatter.compact(it, BODY_MAX_LENGTH))
                }
            }
        }
    }

    private fun appendColored(builder: SpannableStringBuilder, text: String, color: Int, bold: Boolean = false) {
        val start = builder.length
        builder.append(text)
        val end = builder.length
        builder.setSpan(ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        if (bold) builder.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun applyMonospace(builder: SpannableStringBuilder) {
        builder.setSpan(TypefaceSpan("monospace"), 0, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}
