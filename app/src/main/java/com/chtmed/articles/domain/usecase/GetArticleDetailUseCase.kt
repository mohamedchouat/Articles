package com.chtmed.articles.domain.usecase

import com.chtmed.articles.core.util.AppResult
import com.chtmed.articles.domain.model.ArticleDetail
import com.chtmed.articles.domain.repository.ArticleRepository
import javax.inject.Inject

class GetArticleDetailUseCase @Inject constructor(
    private val repository: ArticleRepository
) {
    suspend operator fun invoke(articleId: Int): AppResult<ArticleDetail> {
        return repository.getArticleDetail(articleId)
    }
}
