package com.chtmed.articles.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chtmed.articles.core.util.AppResult
import com.chtmed.articles.domain.usecase.GetArticleDetailUseCase
import com.chtmed.articles.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getArticleDetailUseCase: GetArticleDetailUseCase
) : ViewModel() {

    // Hilt's navigation-compose integration exposes the NavBackStackEntry's
    // arguments through SavedStateHandle automatically, so no manual argument
    // passing/factory is needed to get the clicked article's id here.
    private val articleId: Int =
        checkNotNull(savedStateHandle[Screen.ArticleDetail.ARG_ARTICLE_ID]) {
            "articleId argument is required for ArticleDetailScreen"
        }

    private val _uiState = MutableStateFlow(ArticleDetailUiState())
    val uiState: StateFlow<ArticleDetailUiState> = _uiState.asStateFlow()

    init {
        loadArticleDetail()
    }

    fun onEvent(event: ArticleDetailEvent) {
        when (event) {
            is ArticleDetailEvent.Retry -> loadArticleDetail()
        }
    }

    private fun loadArticleDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = getArticleDetailUseCase(articleId)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, article = result.data, error = null)
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.error)
                    }
                }
            }
        }
    }
}
