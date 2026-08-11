# 📰 Articles App

## 📸 Screenshots

| Articles List | Article Detail | REST API Debugger Notification | REST API Debugger History |
|---|---|---|---|
| ![Articles List](https://github.com/mohamedchouat/Articles/blob/main/screen/list.png) | ![Article Detail](https://github.com/mohamedchouat/Articles/blob/main/screen/details.png) | ![REST API Debugger Notification](https://github.com/mohamedchouat/Articles/blob/main/screen/notif.png) | ![REST API Debugger History](https://github.com/mohamedchouat/Articles/blob/main/screen/api%20page.png) |

[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-orange?style=flat-square)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blue?style=flat-square)](https://developer.android.com/jetpack/compose)
[![Hilt](https://img.shields.io/badge/DI-Hilt-purple?style=flat-square)](https://dagger.dev/hilt/)
[![Room](https://img.shields.io/badge/Persistence-Room-brightgreen?style=flat-square)](https://developer.android.com/training/data-storage/room)

**Articles App** is a Kotlin/Jetpack Compose Android client for the public
[DEV.to REST API](https://developers.forem.com/api/v1). It's built offline-first
around a **single source of truth** (Room), follows **Clean Architecture +
MVVM**, and integrates [**rest-api-debugger**](https://github.com/mohamedchouat/rest-api-debugger),
a standalone library that surfaces every network call through a live
notification and an in-app history/detail UI.

---

## ✨ Features

- 📰 **Articles List** – Browse dev.to articles with pull-to-refresh
- 📖 **Article Detail** – Full article view with sanitized HTML body rendering (WebView)
- 🔁 **Single Source of Truth** – The list only ever changes because Room's
  `Flow` emitted; a refresh is a write-only network sync into the cache, never
  a direct hand-off of network data to the UI
- 📴 **Offline Support** – Room caches both the list and any article you've
  opened, so returning without internet still shows the last known data
- 🔄 **Reload on Return** – Coming back from the detail screen re-syncs the
  list automatically (tracked on the ViewModel, so it survives Navigation
  Compose disposing/rebuilding the screen's composition)
- 🎨 **Custom Editorial Design** – Material 3 theme with a bundled variable
  font (Plus Jakarta Sans) and a dedicated color/shape system
- 🐞 **REST API Debugger** – integrates [rest-api-debugger](https://github.com/mohamedchouat/rest-api-debugger),
  which intercepts every OkHttp call and shows it via a live, color-coded
  notification plus a Compose history/detail UI
- 🔒 **Secret Masking** – Authorization headers, tokens, passwords, cookies,
  etc. are automatically redacted before the debugger ever stores or displays them
- 🛠️ **MVVM + Clean Architecture** – strict domain → data → presentation layering
- ⚡ **Hilt DI** throughout, including the local Room database

---

## 🏗️ Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Architecture:** Clean Architecture + MVVM, single-source-of-truth repository pattern
- **Dependency Injection:** Hilt
- **Networking:** Retrofit + OkHttp + kotlinx.serialization
- **Persistence:** Room (offline cache for both screens)
- **Images:** Coil
- **Navigation:** Navigation Compose
- **Debugging:** [rest-api-debugger](https://github.com/mohamedchouat/rest-api-debugger)
  (own repo, consumed via JitPack) — OkHttp interceptor, `NotificationCompat`,
  and its own Compose UI
- **Tests:** JUnit, MockK, Turbine, `kotlinx-coroutines-test`

---

## 📂 Project Structure

A single Gradle module (`app`). The REST API debugger used to live here as
a second module; it's now [its own repo](https://github.com/mohamedchouat/rest-api-debugger),
consumed as a published Maven artifact via JitPack — see the REST API
Debugger section below.

```plaintext
Articles
│
├── app
│   └── src/main/java/com/chtmed/articles
│       │   ArticlesApplication.kt        # RestApiDebugger.initialize(...)
│       │   MainActivity.kt
│       │
│       ├── core/util
│       │   ├── AppResult.kt              # AppResult/AppError sealed types
│       │   └── DispatcherQualifiers.kt
│       │
│       ├── domain
│       │   ├── model
│       │   │   ├── Article.kt
│       │   │   └── ArticleDetail.kt
│       │   ├── repository
│       │   │   └── ArticleRepository.kt
│       │   └── usecase
│       │       ├── ObserveArticlesUseCase.kt     # reads Room only
│       │       ├── RefreshArticlesUseCase.kt     # network -> Room, write-only
│       │       └── GetArticleDetailUseCase.kt
│       │
│       ├── data
│       │   ├── remote
│       │   │   ├── api/DevToApiService.kt
│       │   │   └── dto/                          # ArticleDto, UserDto, FlexibleTagListSerializer
│       │   ├── local
│       │   │   ├── ArticleEntity.kt
│       │   │   ├── ArticleDao.kt                 # observeArticles(): Flow<...>
│       │   │   ├── ArticlesDatabase.kt
│       │   │   └── Converters.kt
│       │   ├── mapper/ArticleMapper.kt
│       │   └── repository/ArticleRepositoryImpl.kt
│       │
│       ├── presentation
│       │   ├── list/                     # ArticlesListScreen + ViewModel + Contract
│       │   ├── detail/                   # ArticleDetailScreen + ViewModel + Contract
│       │   ├── components/               # LoadingView, ErrorView
│       │   ├── navigation/                # ArticlesNavGraph, Screen
│       │   └── theme/                    # Color, Type (Plus Jakarta Sans), Shape, Theme
│       │
│       └── di
│           ├── NetworkModule.kt          # Retrofit/OkHttp + RestApiDebugger.interceptor()
│           ├── DatabaseModule.kt
│           ├── RepositoryModule.kt
│           └── DispatcherModule.kt
```

---

## 🐞 REST API Debugger

Every request/response made through the app's `OkHttpClient` is captured by
[rest-api-debugger](https://github.com/mohamedchouat/rest-api-debugger) —
method, URL, headers, request/response bodies, status, and timing — with
secrets masked before anything is stored. It's a separate repo/library,
pulled in here via JitPack:

```kotlin
// settings.gradle.kts
maven("https://jitpack.io")

// app/build.gradle.kts
implementation("com.github.mohamedchouat:rest-api-debugger:v1.0.0")
```

and controlled by a Gradle flag in this app:

```properties
# gradle.properties
debugRestApi=true
```

which is wired into `BuildConfig.DEBUG_REST_API` (always `false` in release
builds regardless of the flag). Tap the notification to open the full
history, then tap a call for its full request/response detail.

See the library's own [README](https://github.com/mohamedchouat/rest-api-debugger)
for its full documentation, security/masking details, and a sample app with
GET/POST/PUT/DELETE demo buttons.

---

## 🚀 Running it

1. Open the project root in **Android Studio (Koala or newer)**.
2. Let Gradle sync (requires network access to `google()` and `mavenCentral()`).
3. Run the `app` configuration on an emulator or device (minSdk 24).

No API keys or `.env` setup needed — the DEV.to endpoints used are public.

## 🧪 Running tests

```
./gradlew testDebugUnitTest
```
