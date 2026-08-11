package com.chtmed.articles.domain.repository

import com.chtmed.articles.core.util.AppResult
import com.chtmed.articles.domain.model.Article
import com.chtmed.articles.domain.model.ArticleDetail
import kotlinx.coroutines.flow.Flow

/**
 * Domain-facing contract for article data. The domain layer only knows about
 * this interface — never about Retrofit, DTOs, or any data-layer detail.
 * The implementation lives in the data layer (see ArticleRepositoryImpl)
 * and is bound via Hilt (see di/RepositoryModule).
 *
 * The local cache (Room) is the single source of truth for the article list:
 * [observeArticles] always reads from it, and [refreshArticles] is the only
 * way to pull fresh data from the network into it. The UI layer never sees
 * network results directly — it reacts to whatever changes in the cache.
 */
interface ArticleRepository {

    fun observeArticles(): Flow<List<Article>>

    suspend fun refreshArticles(page: Int = 1, perPage: Int = 30): AppResult<Unit>

    suspend fun getArticleDetail(articleId: Int): AppResult<ArticleDetail>
}
