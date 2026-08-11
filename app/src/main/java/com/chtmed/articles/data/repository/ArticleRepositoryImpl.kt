package com.chtmed.articles.data.repository

import com.chtmed.articles.core.util.AppError
import com.chtmed.articles.core.util.AppResult
import com.chtmed.articles.core.util.IoDispatcher
import com.chtmed.articles.data.local.ArticleDao
import com.chtmed.articles.data.mapper.toDetailDomain
import com.chtmed.articles.data.mapper.toDomain
import com.chtmed.articles.data.mapper.toEntity
import com.chtmed.articles.data.remote.api.DevToApiService
import com.chtmed.articles.domain.model.Article
import com.chtmed.articles.domain.model.ArticleDetail
import com.chtmed.articles.domain.repository.ArticleRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

/**
 * Concrete implementation of [ArticleRepository]. This is the only place in
 * the app that talks to Retrofit directly; everything above it (use cases,
 * ViewModels, UI) only ever sees domain models and AppResult/AppError.
 *
 * The article list follows a single-source-of-truth pattern: [observeArticles]
 * only ever reads from Room, and [refreshArticles] is a write-only network
 * sync that updates Room — it does not hand data back to its caller. Any
 * write to the cache (a list refresh, or a detail fetch caching a full
 * article) is picked up automatically by anyone observing [observeArticles],
 * since Room re-emits the query whenever the underlying table changes.
 *
 * [getArticleDetail] stays request/response (with a Room fallback for
 * offline viewing) since only one screen ever needs a given article's detail
 * at a time — there's no shared stream worth maintaining for it.
 */
class ArticleRepositoryImpl @Inject constructor(
    private val api: DevToApiService,
    private val dao: ArticleDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ArticleRepository {

    override fun observeArticles(): Flow<List<Article>> =
        dao.observeArticles()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    override suspend fun refreshArticles(page: Int, perPage: Int): AppResult<Unit> =
        withContext(ioDispatcher) {
            val result = safeApiCall {
                api.getArticles(page = page, perPage = perPage).map { it.toDomain() }
            }
            when (result) {
                is AppResult.Success -> {
                    dao.upsertArticles(result.data.mapIndexed { index, article -> article.toEntity(index) })
                    AppResult.Success(Unit)
                }
                is AppResult.Error -> result
            }
        }

    override suspend fun getArticleDetail(articleId: Int): AppResult<ArticleDetail> =
        withContext(ioDispatcher) {
            val result = safeApiCall {
                api.getArticleDetail(articleId).toDetailDomain()
            }
            when (result) {
                is AppResult.Success -> {
                    val listOrder = dao.getArticleById(articleId)?.listOrder ?: 0
                    dao.upsertArticle(result.data.toEntity(listOrder))
                    result
                }
                is AppResult.Error -> {
                    val cached = dao.getArticleById(articleId)?.toDetailDomain()
                    if (cached != null) AppResult.Success(cached) else result
                }
            }
        }

    /**
     * Centralizes exception -> AppError translation so every repository method
     * handles errors identically, and low-level exception types never leak
     * past this layer.
     */
    private inline fun <T> safeApiCall(block: () -> T): AppResult<T> {
        return try {
            AppResult.Success(block())
        } catch (e: SocketTimeoutException) {
            AppResult.Error(AppError.Timeout)
        } catch (e: IOException) {
            AppResult.Error(AppError.NoInternet)
        } catch (e: HttpException) {
            AppResult.Error(AppError.Server(e.code()))
        } catch (e: Exception) {
            AppResult.Error(AppError.Unknown(e.message))
        }
    }
}
