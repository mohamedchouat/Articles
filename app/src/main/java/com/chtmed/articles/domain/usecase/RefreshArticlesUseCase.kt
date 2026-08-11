package com.chtmed.articles.domain.usecase

import com.chtmed.articles.core.util.AppResult
import com.chtmed.articles.domain.repository.ArticleRepository
import javax.inject.Inject

/**
 * Fetches the latest articles from the network and writes them into the
 * local cache. It intentionally returns [AppResult]<Unit> rather than the
 * fetched articles — callers get data by observing [ObserveArticlesUseCase],
 * not from this call's return value, so there is exactly one path the UI
 * ever reads articles from.
 */
class RefreshArticlesUseCase @Inject constructor(
    private val repository: ArticleRepository
) {
    suspend operator fun invoke(page: Int = 1, perPage: Int = 30): AppResult<Unit> =
        repository.refreshArticles(page = page, perPage = perPage)
}
