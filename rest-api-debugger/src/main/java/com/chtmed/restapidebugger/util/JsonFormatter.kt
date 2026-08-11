package com.chtmed.restapidebugger.util

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/** Pretty-prints JSON bodies for display; non-JSON bodies pass through unchanged. */
object JsonFormatter {
    private const val INDENT_SPACES = 4

    fun prettyPrint(raw: String?): String {
        if (raw.isNullOrBlank()) return raw.orEmpty()
        val trimmed = raw.trim()
        return try {
            when {
                trimmed.startsWith("{") -> JSONObject(trimmed).toString(INDENT_SPACES)
                trimmed.startsWith("[") -> JSONArray(trimmed).toString(INDENT_SPACES)
                else -> raw
            }
        } catch (e: JSONException) {
            raw
        }
    }

    /** A single-line, size-capped rendering used for notification text. */
    fun compact(raw: String?, maxLength: Int = 300): String {
        if (raw.isNullOrBlank()) return ""
        val oneLine = raw.replace(Regex("\\s+"), " ").trim()
        return if (oneLine.length > maxLength) oneLine.take(maxLength) + "…" else oneLine
    }
}
