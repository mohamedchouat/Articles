package com.chtmed.restapidebugger.interceptor

import com.chtmed.restapidebugger.RestApiDebugger
import com.chtmed.restapidebugger.model.ApiCallRecord
import com.chtmed.restapidebugger.notification.ApiDebuggerNotifier
import com.chtmed.restapidebugger.store.ApiCallHistoryStore
import com.chtmed.restapidebugger.util.SensitiveDataMasker
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.io.IOException
import java.util.UUID

/**
 * Application-level OkHttp interceptor that captures request/response
 * metadata for the debugger. When [RestApiDebugger] is disabled this is a
 * single volatile-boolean check and an immediate `chain.proceed()` — no
 * buffering, masking, storage, or notification work happens at all.
 */
internal class ApiCallInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!RestApiDebugger.isEnabled) {
            return chain.proceed(request)
        }

        val id = UUID.randomUUID().toString()
        val requestTimestamp = System.currentTimeMillis()
        val requestBody = peekRequestBody(request)
        val startNanos = System.nanoTime()

        val response = try {
            chain.proceed(request)
        } catch (e: IOException) {
            recordAndNotify(
                id = id,
                request = request,
                requestBody = requestBody,
                requestTimestamp = requestTimestamp,
                durationMs = elapsedMs(startNanos),
                response = null,
                responseBody = null,
                errorMessage = e.message ?: e.javaClass.simpleName
            )
            throw e
        }

        val responseBody = peekResponseBody(response)
        recordAndNotify(
            id = id,
            request = request,
            requestBody = requestBody,
            requestTimestamp = requestTimestamp,
            durationMs = elapsedMs(startNanos),
            response = response,
            responseBody = responseBody,
            errorMessage = null
        )
        return response
    }

    private fun recordAndNotify(
        id: String,
        request: Request,
        requestBody: String?,
        requestTimestamp: Long,
        durationMs: Long,
        response: Response?,
        responseBody: String?,
        errorMessage: String?
    ) {
        val record = ApiCallRecord(
            id = id,
            method = request.method,
            url = request.url.toString(),
            path = request.url.encodedPath,
            queryParams = request.url.queryParameterNames.associateWith { name ->
                request.url.queryParameter(name).orEmpty()
            },
            requestHeaders = SensitiveDataMasker.maskHeaders(request.headers.toMultimap()),
            requestBody = SensitiveDataMasker.maskJsonBody(requestBody),
            responseHeaders = response?.headers?.toMultimap()?.let { SensitiveDataMasker.maskHeaders(it) }.orEmpty(),
            statusCode = response?.code,
            statusMessage = response?.message,
            responseBody = SensitiveDataMasker.maskJsonBody(responseBody),
            requestTimestamp = requestTimestamp,
            responseTimestamp = if (response != null) requestTimestamp + durationMs else null,
            durationMs = durationMs,
            isError = response == null || !response.isSuccessful,
            errorMessage = errorMessage
        )
        ApiCallHistoryStore.add(record)
        ApiDebuggerNotifier.onNewCall(record)
    }

    private fun elapsedMs(startNanos: Long): Long = (System.nanoTime() - startNanos) / 1_000_000

    /**
     * Peeks the request body without disturbing the actual send. One-shot
     * bodies (real single-use streams) are skipped entirely — writing them
     * here to "peek" would consume the stream the real network write still
     * needs, exactly the hazard OkHttp's own HttpLoggingInterceptor guards
     * against.
     */
    private fun peekRequestBody(request: Request): String? {
        val body = request.body ?: return null
        if (body.isOneShot()) return "<streaming request body — not captured>"
        if (!isReadableContentType(body.contentType())) return "<binary body, ${body.contentLength()} bytes>"
        return try {
            val buffer = Buffer()
            body.writeTo(buffer)
            val charset = body.contentType()?.charset(Charsets.UTF_8) ?: Charsets.UTF_8
            buffer.readString(charset)
        } catch (e: IOException) {
            "<unable to read request body: ${e.message}>"
        }
    }

    /** Uses OkHttp's non-destructive peekBody so the real response consumer downstream is unaffected. */
    private fun peekResponseBody(response: Response): String? {
        val body = response.body ?: return null
        if (!isReadableContentType(body.contentType())) return "<binary body, ${body.contentLength()} bytes>"
        return try {
            response.peekBody(MAX_PEEK_BYTES).string()
        } catch (e: IOException) {
            "<unable to read response body: ${e.message}>"
        }
    }

    private fun isReadableContentType(contentType: MediaType?): Boolean {
        if (contentType == null) return true
        val type = contentType.type
        val subtype = contentType.subtype
        return type == "text" ||
            (type == "application" &&
                (subtype.contains("json") || subtype.contains("xml") ||
                    subtype.contains("javascript") || subtype == "x-www-form-urlencoded"))
    }

    private fun Headers.toMultimap(): Map<String, List<String>> =
        names().associateWith { name -> values(name) }

    private companion object {
        const val MAX_PEEK_BYTES = 1L * 1024 * 1024 // 1 MB cap per body
    }
}
