package com.chtmed.restapidebugger.util

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Strips anything that looks like a credential before a request/response is
 * ever stored or displayed. This runs unconditionally on every captured
 * call — there is no "verbose mode" that skips it.
 */
object SensitiveDataMasker {
    private const val MASK = "********"

    private val SENSITIVE_HEADER_NAMES = setOf(
        "authorization",
        "proxy-authorization",
        "cookie",
        "set-cookie",
        "x-api-key",
        "api-key",
        "x-auth-token"
    )

    private val SENSITIVE_BODY_KEYS = setOf(
        "password", "pass", "pwd",
        "token", "accesstoken", "access_token",
        "refreshtoken", "refresh_token",
        "idtoken", "id_token",
        "secret", "clientsecret", "client_secret",
        "apikey", "api_key",
        "authorization", "auth",
        "creditcard", "credit_card", "cardnumber", "card_number", "cvv", "cvc",
        "ssn"
    )

    fun maskHeaders(headers: Map<String, List<String>>): Map<String, String> =
        headers.mapValues { (name, values) ->
            if (SENSITIVE_HEADER_NAMES.contains(name.lowercase())) MASK else values.joinToString(", ")
        }

    /** Masks sensitive fields inside a JSON object/array body; non-JSON bodies pass through. */
    fun maskJsonBody(raw: String?): String? {
        if (raw.isNullOrBlank()) return raw
        val trimmed = raw.trim()
        return try {
            when {
                trimmed.startsWith("{") -> maskObject(JSONObject(trimmed)).toString()
                trimmed.startsWith("[") -> maskArray(JSONArray(trimmed)).toString()
                else -> maskFormUrlEncoded(raw)
            }
        } catch (e: JSONException) {
            maskFormUrlEncoded(raw)
        }
    }

    private fun maskObject(json: JSONObject): JSONObject {
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            if (SENSITIVE_BODY_KEYS.contains(key.lowercase())) {
                json.put(key, MASK)
                continue
            }
            when (val value = json.opt(key)) {
                is JSONObject -> json.put(key, maskObject(value))
                is JSONArray -> json.put(key, maskArray(value))
                else -> Unit
            }
        }
        return json
    }

    private fun maskArray(array: JSONArray): JSONArray {
        for (i in 0 until array.length()) {
            when (val value = array.opt(i)) {
                is JSONObject -> array.put(i, maskObject(value))
                is JSONArray -> array.put(i, maskArray(value))
                else -> Unit
            }
        }
        return array
    }

    private val FORM_ENCODED_PATTERN = Regex("^[^\\s=&]+=[^&]*(&[^\\s=&]+=[^&]*)*$")

    /** Best-effort masking for application/x-www-form-urlencoded or plain "key=value&..." bodies. */
    private fun maskFormUrlEncoded(raw: String): String {
        if (!FORM_ENCODED_PATTERN.matches(raw)) return raw
        return raw.split("&").joinToString("&") { pair ->
            val idx = pair.indexOf("=")
            if (idx == -1) return@joinToString pair
            val key = pair.substring(0, idx)
            if (SENSITIVE_BODY_KEYS.contains(key.lowercase())) "$key=$MASK" else pair
        }
    }
}
