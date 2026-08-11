package com.chtmed.restapidebugger.model

/**
 * A single captured REST API call, request and response combined. Header/body
 * values on this record have already been through [com.chtmed.restapidebugger.util.SensitiveDataMasker]
 * by the time they reach here — nothing sensitive is ever stored or displayed.
 */
data class ApiCallRecord(
    val id: String,
    val method: String,
    val url: String,
    val path: String,
    val queryParams: Map<String, String>,
    val requestHeaders: Map<String, String>,
    val requestBody: String?,
    val responseHeaders: Map<String, String>,
    val statusCode: Int?,
    val statusMessage: String?,
    val responseBody: String?,
    val requestTimestamp: Long,
    val responseTimestamp: Long?,
    val durationMs: Long?,
    val isError: Boolean,
    val errorMessage: String?
) {
    val isRequestBearingMethod: Boolean
        get() = method.equals("POST", ignoreCase = true) ||
            method.equals("PUT", ignoreCase = true) ||
            method.equals("PATCH", ignoreCase = true)
}
