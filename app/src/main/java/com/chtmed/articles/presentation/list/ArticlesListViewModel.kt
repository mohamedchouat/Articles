package com.chtmed.articles.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chtmed.articles.core.util.AppResult
import com.chtmed.articles.domain.usecase.ObserveArticlesUseCase
import com.chtmed.articles.domain.usecase.RefreshArticlesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticlesListViewModel @Inject constructor(
    private val observeArticlesUseCase: ObserveArticlesUseCase,
    private val refreshArticlesUseCase: RefreshArticlesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticlesListUiState())
    val uiState: StateFlow<ArticlesListUiState> = _uiState.asStateFlow()

    // Tracks whether the screen has already been resumed once, so a second
    // resume (returning from the detail screen) triggers a refresh while the
    // very first one (this screen's initial appearance) doesn't duplicate
    // init{}'s own load. This has to live here rather than in the Composable:
    // Navigation-Compose disposes and rebuilds the list screen's composition
    // when you navigate away to detail, so any local (remember/composition-
    // scoped) flag would reset on the way back — this ViewModel instance is
    // the only thing that actually survives that round trip.
    private var hasResumedBefore = false

    init {
        // Room is the single source of truth for what's rendered: this stays
        // collected for the ViewModel's whole lifetime, so any write to the
        // cache — from refresh() below, or from the detail screen caching a
        // fully-fetched article — updates the list on its own.
        observeArticles()
        refresh(showFullScreenLoader = true)
    }

    fun onEvent(event: ArticlesListEvent) {
        when (event) {
            is ArticlesListEvent.LoadArticles -> refresh(showFullScreenLoader = true)
            is ArticlesListEvent.Refresh -> refresh(isRefresh = true)
            is ArticlesListEvent.Retry -> refresh(showFullScreenLoader = true)
        }
    }

    /** Call on the screen's ON_RESUME lifecycle event, including its very first one. */
    fun onScreenResumed() {
        if (hasResumedBefore) {
            refresh(isRefresh = true)
        }
        hasResumedBefore = true
    }

    private fun observeArticles() {
        viewModelScope.launch {
            observeArticlesUseCase().collectLatest { articles ->
                _uiState.update { it.copy(articles = articles) }
            }
        }
    }

    /**
     * Write-only network sync: on success/error this only ever updates
     * loading/error flags, never `articles` directly — the list itself only
     * ever changes because [observeArticles] saw the cache change.
     */
    private fun refresh(
        showFullScreenLoader: Boolean = false,
        isRefresh: Boolean = false
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = showFullScreenLoader && it.articles.isEmpty(),
                    isRefreshing = isRefresh,
                    error = null
                )
            }

            when (val result = refreshArticlesUseCase()) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = null) }
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = result.error) }
                }
            }
        }
    }
}
