package com.chtmed.articles.presentation.detail

import com.chtmed.articles.core.util.AppError
import com.chtmed.articles.domain.model.ArticleDetail

data class ArticleDetailUiState(
    val isLoading: Boolean = false,
    val article: ArticleDetail? = null,
    val error: AppError? = null
)

sealed interface ArticleDetailEvent {
    data object Retry : ArticleDetailEvent
}
