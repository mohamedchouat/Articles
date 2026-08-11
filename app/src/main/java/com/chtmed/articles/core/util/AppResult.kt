package com.chtmed.articles.core.util

/**
 * A generic, framework-agnostic wrapper for operations that can succeed or fail.
 * Named `AppResult` (instead of `Result`) to avoid clashing with kotlin.Result.
 *
 * Using a sealed type instead of throwing exceptions across layers keeps the
 * domain/presentation boundary explicit and forces callers to handle errors.
 */
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val error: AppError) : AppResult<Nothing>
}

/**
 * Domain-level classification of errors. The data layer is responsible for
 * mapping low-level exceptions (IOException, HttpException, etc.) into these,
 * so the presentation layer never needs to know about Retrofit/OkHttp types.
 */
sealed interface AppError {
    data object NoInternet : AppError
    data object Timeout : AppError
    data class Server(val code: Int) : AppError
    data class Unknown(val message: String?) : AppError
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Error -> this
}
