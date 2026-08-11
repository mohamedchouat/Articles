package com.chtmed.articles.data.remote.api

import com.chtmed.articles.data.remote.dto.ArticleDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * DEV.to (Forem) public REST API. No authentication required for these two
 * read-only endpoints. Docs: https://developers.forem.com/api/v1
 *
 * Base URL: https://dev.to/api/
 */
interface DevToApiService {

    @GET("articles")
    suspend fun getArticles(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): List<ArticleDto>

    @GET("articles/{id}")
    suspend fun getArticleDetail(
        @Path("id") id: Int
    ): ArticleDto
}
