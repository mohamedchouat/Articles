package com.chtmed.articles.core.util

import javax.inject.Qualifier

/**
 * Qualifiers used to inject specific CoroutineDispatchers instead of referencing
 * Dispatchers.IO/Main directly in classes. This lets tests substitute a
 * TestDispatcher via Hilt without touching production code.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher
