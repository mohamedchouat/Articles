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

This repo itself now consumes it exactly the way an external project would —
as a published Maven artifact via [JitPack](https://jitpack.io/#mohamedchouat/Articles),
not a `project(":rest-api-debugger")` reference — so `app/build.gradle.kts`
and `settings.gradle.kts` here double as a working, verified example.

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("com.github.mohamedchouat:Articles:<tag>")

    // The module declares OkHttp as compileOnly, precisely so it doesn't
    // force a specific OkHttp version on consumers — bring your own:
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
```

Replace `<tag>` with a published release tag (e.g. `v1.0.0`), a commit hash,
or `main-SNAPSHOT` for the latest commit on `main`.

**On the coordinate:** `rest-api-debugger/build.gradle.kts` declares an
explicit `groupId`/`artifactId` (`com.github.mohamedchouat.Articles:rest-api-debugger`)
for its Maven publication, which is the right pattern *if* this repo ever
publishes more than one library module — JitPack then exposes each one at
`com.github.<user>.<repo>:<module>`. Today `rest-api-debugger` is the only
publishable module in the repo, so JitPack collapses it to the plain
repo-level coordinate instead: `com.github.mohamedchouat:Articles:<tag>`.
If that stops resolving after a second library module is added here, switch
back to the module-scoped coordinate.

The very first build of a given tag can take JitPack a minute or two — a
fresh `implementation(...)` resolve may 404 until that finishes. Check
[jitpack.io/#mohamedchouat/Articles](https://jitpack.io/#mohamedchouat/Articles)
for build status, or trigger it by opening that page, if resolution fails.

Note the group id joins the GitHub username and repo with a dot
(`com.github.mohamedchouat.Articles`), not a colon — that's JitPack's
convention for resolving a specific module out of a multi-module repo like
this one (`app` + `rest-api-debugger`), as opposed to `com.github.User:Repo`
for single-artifact repos.

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
