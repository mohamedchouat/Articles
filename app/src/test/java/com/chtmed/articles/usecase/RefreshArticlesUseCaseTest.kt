package com.chtmed.articles.usecase

import com.chtmed.articles.core.util.AppError
import com.chtmed.articles.core.util.AppResult
import com.chtmed.articles.domain.repository.ArticleRepository
import com.chtmed.articles.domain.usecase.RefreshArticlesUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RefreshArticlesUseCaseTest {

    private lateinit var repository: ArticleRepository
    private lateinit var useCase: RefreshArticlesUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = RefreshArticlesUseCase(repository)
    }

    @Test
    fun `invoke returns success when repository refresh succeeds`() = runTest {
        coEvery { repository.refreshArticles(page = 1, perPage = 30) } returns AppResult.Success(Unit)

        val result = useCase(page = 1, perPage = 30)

        assertTrue(result is AppResult.Success)
    }

    @Test
    fun `invoke propagates error from repository`() = runTest {
        coEvery { repository.refreshArticles(page = 1, perPage = 30) } returns
            AppResult.Error(AppError.NoInternet)

        val result = useCase(page = 1, perPage = 30)

        assertTrue(result is AppResult.Error)
        assertEquals(AppError.NoInternet, (result as AppResult.Error).error)
    }

    @Test
    fun `invoke defaults to page 1 and perPage 30`() = runTest {
        coEvery { repository.refreshArticles(page = 1, perPage = 30) } returns AppResult.Success(Unit)

        useCase()

        coVerify(exactly = 1) { repository.refreshArticles(page = 1, perPage = 30) }
    }
}
