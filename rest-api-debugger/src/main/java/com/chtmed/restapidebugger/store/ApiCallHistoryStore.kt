package com.chtmed.restapidebugger.store

import com.chtmed.restapidebugger.model.ApiCallRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Bounded in-memory history of captured calls, newest first. Nothing here is
 * ever persisted to disk — the history disappears when the process dies,
 * which is the right lifetime for a debugging tool.
 */
object ApiCallHistoryStore {
    private const val MAX_HISTORY_SIZE = 200

    private val _history = MutableStateFlow<List<ApiCallRecord>>(emptyList())
    val history: StateFlow<List<ApiCallRecord>> = _history.asStateFlow()

    /** MutableStateFlow.update uses an atomic compare-and-set loop, so concurrent
     *  calls from different OkHttp dispatcher threads are safe without extra locking. */
    fun add(record: ApiCallRecord) {
        _history.update { current -> (listOf(record) + current).take(MAX_HISTORY_SIZE) }
    }

    fun get(id: String): ApiCallRecord? = _history.value.firstOrNull { it.id == id }

    fun clear() {
        _history.value = emptyList()
    }
}
