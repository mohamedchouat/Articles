package com.chtmed.articles.usecase

import com.chtmed.articles.domain.model.Article
import com.chtmed.articles.domain.repository.ArticleRepository
import com.chtmed.articles.domain.usecase.ObserveArticlesUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveArticlesUseCaseTest {

    private val sampleArticle = Article(
        id = 1,
        title = "Sample title",
        description = "Sample description",
        coverImageUrl = null,
        authorName = "Author",
        authorAvatarUrl = null,
        publishedAt = "Aug 9",
        tags = listOf("kotlin"),
        readingTimeMinutes = 5,
        commentsCount = 0,
        reactionsCount = 0
    )

    @Test
    fun `invoke returns the repository's article cache flow unchanged`() = runTest {
        val repository: ArticleRepository = mockk()
        every { repository.observeArticles() } returns flowOf(listOf(sampleArticle))

        val useCase = ObserveArticlesUseCase(repository)

        assertEquals(listOf(sampleArticle), useCase().first())
    }
}
