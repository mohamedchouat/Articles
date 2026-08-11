# rest-api-debugger

A self-contained Android library that captures REST API calls made through an
existing OkHttp/Retrofit setup and surfaces them through a live notification
and an in-app history/detail UI, for debug builds only.

## What it does

- Intercepts every request/response via a standard `okhttp3.Interceptor` —
  no changes to Retrofit API interfaces.
- Records method, full URL, query params, headers, request/response bodies,
  status, timing, and errors, keeping the last 200 calls in memory.
- Masks anything that looks like a credential (Authorization headers,
  cookies, tokens, passwords, API keys, ...) before it is ever stored,
  logged, or displayed.
- Shows a single, continuously-updated notification with color-coded,
  monospaced call summaries; tapping it opens the full history screen.
- Provides a Compose history list and a call-detail screen (headers, body,
  status, timing) reachable from that notification.
- Does effectively nothing when disabled: one `Boolean` check and an
  immediate `chain.proceed()`, no capturing/masking/storage/notification
  work at all.

## Integration

### 1. Add the module

Already wired into this project (`settings.gradle.kts` includes
`:rest-api-debugger`, and `app/build.gradle.kts` depends on it). For a new
project, copy the module folder, add it to `settings.gradle.kts`, and add
`implementation(project(":rest-api-debugger"))` to the consuming app module.

### 2. Gradle toggle

`app/build.gradle.kts` exposes a `debugRestApi` Gradle property (defaulted in
`gradle.properties`) that controls the debug build's `BuildConfig` flag.
Release builds always hard-disable it, regardless of the property:

```kotlin
val debugRestApiEnabled = (project.findProperty("debugRestApi") as? String)?.toBoolean() ?: true

android {
    buildTypes {
        debug {
            buildConfigField("boolean", "DEBUG_REST_API", debugRestApiEnabled.toString())
        }
        release {
            buildConfigField("boolean", "DEBUG_REST_API", "false")
        }
    }
}
```

Flip it off for a single build with `./gradlew assembleDebug -PdebugRestApi=false`,
or permanently in `gradle.properties`.

### 3. Initialize once

```kotlin
// ArticlesApplication.onCreate()
RestApiDebugger.initialize(this, BuildConfig.DEBUG_REST_API)
```

### 4. Add the interceptor

```kotlin
// wherever the app's OkHttpClient.Builder is configured (NetworkModule)
OkHttpClient.Builder()
    .addInterceptor(RestApiDebugger.interceptor())
    .build()
```

`RestApiDebugger.interceptor()` always returns a valid `Interceptor` — safe
to leave wired in release builds too, since it becomes a no-op there.

### 5. (Recommended) Request the notification permission on Android 13+

Posting a notification requires `POST_NOTIFICATIONS` at runtime on API 33+.
This is a normal Activity-level permission request, so the library can't do
it from `Application.onCreate()` — the host app requests it, typically once
from its launcher Activity, gated behind the same debug flag:

```kotlin
if (BuildConfig.DEBUG_REST_API && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    // ActivityResultContracts.RequestPermission() launcher, see MainActivity.kt
}
```

Without this, the debugger still records everything and the history screen
is still reachable directly (`RestApiDebugger.historyScreenIntent(context)`);
only the notification itself won't appear.

## Opening the debugger UI directly

```kotlin
startActivity(RestApiDebugger.historyScreenIntent(context))
```

## Security

`SensitiveDataMasker` runs unconditionally on every captured header and JSON
body — there is no verbose/insecure mode. It masks (case-insensitive):

- Headers: `Authorization`, `Proxy-Authorization`, `Cookie`, `Set-Cookie`,
  `X-Api-Key`, `Api-Key`, `X-Auth-Token`
- JSON/form body keys: `password`, `token`, `access_token`, `refresh_token`,
  `id_token`, `secret`, `client_secret`, `api_key`, `authorization`,
  `credit_card`, `card_number`, `cvv`/`cvc`, `ssn`, and common variants

Matched values are replaced with `********`. This is a denylist, so any
project-specific secret field names should be added to
`SensitiveDataMasker.SENSITIVE_BODY_KEYS`.

## Notes on request/response body capture

- Response bodies are read via OkHttp's `Response.peekBody()`, which does
  not consume the stream Retrofit's converter reads afterwards.
- Request bodies are peeked via a throwaway `Buffer`, except **one-shot**
  bodies (`RequestBody.isOneShot() == true`), which are skipped entirely —
  peeking those would consume the stream the real network write still
  needs. This mirrors the same safeguard in OkHttp's own
  `HttpLoggingInterceptor`.
- Bodies are capped at 1 MB and non-text content types (images, octet-stream,
  etc.) are recorded as a size placeholder instead of being read.
