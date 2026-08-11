package com.chtmed.articles.presentation.list

import com.chtmed.articles.core.util.AppError
import com.chtmed.articles.domain.model.Article

/**
 * Single immutable UI state exposed by ArticlesListViewModel. Modeling
 * loading/success/error as one state (rather than three separate booleans)
 * makes illegal states (e.g. loading AND error at once) unrepresentable.
 */
data class ArticlesListUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val articles: List<Article> = emptyList(),
    val error: AppError? = null
) {
    val isEmpty: Boolean get() = !isLoading && error == null && articles.isEmpty()
}

/** One-shot user intents the screen can send to the ViewModel. */
sealed interface ArticlesListEvent {
    data object LoadArticles : ArticlesListEvent
    data object Refresh : ArticlesListEvent
    data object Retry : ArticlesListEvent
}
