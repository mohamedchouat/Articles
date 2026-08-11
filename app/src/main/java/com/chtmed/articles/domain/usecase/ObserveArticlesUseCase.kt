package com.chtmed.articles.domain.usecase

import com.chtmed.articles.domain.model.Article
import com.chtmed.articles.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes the locally cached articles that back the list screen. Room is
 * the single source of truth here — this never talks to the network itself;
 * call [RefreshArticlesUseCase] to pull fresh data into the cache this observes.
 */
class ObserveArticlesUseCase @Inject constructor(
    private val repository: ArticleRepository
) {
    operator fun invoke(): Flow<List<Article>> = repository.observeArticles()
}
