package com.chtmed.restapidebugger.util

/**
 * ARGB color ints shared by the notification (android.graphics spans) and
 * the Compose UI (wrapped via androidx.compose.ui.graphics.Color(Int)).
 */
object DebuggerColors {
    const val METHOD_GET = 0xFF2196F3.toInt()   // blue
    const val METHOD_POST = 0xFF43A047.toInt()  // green
    const val METHOD_PUT_PATCH = 0xFFFB8C00.toInt() // orange
    const val METHOD_DELETE = 0xFFE53935.toInt()    // red
    const val METHOD_OTHER = 0xFF757575.toInt()     // gray

    const val STATUS_2XX = 0xFF43A047.toInt()   // green
    const val STATUS_3XX = 0xFFFB8C00.toInt()   // orange
    const val STATUS_4XX = 0xFFE53935.toInt()   // red
    const val STATUS_5XX = 0xFF8B0000.toInt()   // dark red
    const val STATUS_UNKNOWN = 0xFF757575.toInt() // gray (network error, no response)

    const val DURATION = 0xFF9E9E9E.toInt() // gray

    fun forMethod(method: String): Int = when (method.uppercase()) {
        "GET" -> METHOD_GET
        "POST" -> METHOD_POST
        "PUT", "PATCH" -> METHOD_PUT_PATCH
        "DELETE" -> METHOD_DELETE
        else -> METHOD_OTHER
    }

    fun forStatus(code: Int?): Int = when {
        code == null -> STATUS_UNKNOWN
        code in 200..299 -> STATUS_2XX
        code in 300..399 -> STATUS_3XX
        code in 400..499 -> STATUS_4XX
        code >= 500 -> STATUS_5XX
        else -> STATUS_UNKNOWN
    }
}
