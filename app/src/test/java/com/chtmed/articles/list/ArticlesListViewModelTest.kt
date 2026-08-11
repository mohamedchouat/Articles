package com.chtmed.articles.list

import com.chtmed.articles.core.util.AppError
import com.chtmed.articles.core.util.AppResult
import com.chtmed.articles.domain.model.Article
import com.chtmed.articles.domain.usecase.ObserveArticlesUseCase
import com.chtmed.articles.domain.usecase.RefreshArticlesUseCase
import com.chtmed.articles.presentation.list.ArticlesListEvent
import com.chtmed.articles.presentation.list.ArticlesListViewModel
import com.chtmed.articles.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * The ViewModel follows a single-source-of-truth pattern: `articles` only
 * ever changes because [ObserveArticlesUseCase]'s Flow (standing in for
 * Room) emitted, never as a direct side effect of a refresh call succeeding.
 * These tests simulate Room's behavior by pushing values into the fake cache
 * Flow themselves, and assert on the settled state (via `advanceUntilIdle()`
 * + `uiState.value`) rather than the fleeting mid-refresh frame — with a
 * fully mocked, non-suspending use case call there's no real scheduling gap
 * between the "started" and "finished" state updates for a test to reliably
 * catch in between.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ArticlesListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sampleArticles = listOf(
        Article(
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
    )

    private fun buildViewModel(
        cache: MutableStateFlow<List<Article>>,
        refreshResult: AppResult<Unit>
    ): ArticlesListViewModel {
        val observeArticlesUseCase: ObserveArticlesUseCase = mockk()
        every { observeArticlesUseCase() } returns cache

        val refreshArticlesUseCase: RefreshArticlesUseCase = mockk()
        coEvery { refreshArticlesUseCase(page = 1, perPage = 30) } returns refreshResult

        return ArticlesListViewModel(observeArticlesUseCase, refreshArticlesUseCase)
    }

    @Test
    fun `articles only change because the cache emitted, not because refresh succeeded`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val cache = MutableStateFlow<List<Article>>(emptyList())
            val viewModel = buildViewModel(cache, AppResult.Success(Unit))
            advanceUntilIdle()

            assertTrue(!viewModel.uiState.value.isLoading)
            assertNull(viewModel.uiState.value.error)
            assertTrue(viewModel.uiState.value.articles.isEmpty())

            // Simulate the refresh's Room write landing — this is the only
            // thing that should ever populate `articles`.
            cache.value = sampleArticles
            advanceUntilIdle()

            assertEquals(sampleArticles, viewModel.uiState.value.articles)
        }

    @Test
    fun `initial refresh failure surfaces an error`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val cache = MutableStateFlow<List<Article>>(emptyList())
            val viewModel = buildViewModel(cache, AppResult.Error(AppError.NoInternet))
            advanceUntilIdle()

            assertTrue(!viewModel.uiState.value.isLoading)
            assertTrue(viewModel.uiState.value.articles.isEmpty())
            assertEquals(AppError.NoInternet, viewModel.uiState.value.error)
        }

    @Test
    fun `a refresh failure does not clear articles already served from the cache`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val cache = MutableStateFlow(sampleArticles)
            val viewModel = buildViewModel(cache, AppResult.Error(AppError.NoInternet))
            advanceUntilIdle()

            assertEquals(sampleArticles, viewModel.uiState.value.articles)
            assertEquals(AppError.NoInternet, viewModel.uiState.value.error)
        }

    @Test
    fun `retry event re-triggers a refresh and clears a prior error on success`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val cache = MutableStateFlow<List<Article>>(emptyList())
            val observeArticlesUseCase: ObserveArticlesUseCase = mockk()
            every { observeArticlesUseCase() } returns cache

            val refreshArticlesUseCase: RefreshArticlesUseCase = mockk()
            coEvery { refreshArticlesUseCase(page = 1, perPage = 30) } returns
                AppResult.Error(AppError.NoInternet) andThen
                AppResult.Success(Unit)

            val viewModel = ArticlesListViewModel(observeArticlesUseCase, refreshArticlesUseCase)
            advanceUntilIdle()
            assertEquals(AppError.NoInternet, viewModel.uiState.value.error)

            viewModel.onEvent(ArticlesListEvent.Retry)
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.error)
            assertTrue(!viewModel.uiState.value.isLoading)
        }

    @Test
    fun `first screen resume is a no-op, but a second resume (returning from detail) refreshes`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val cache = MutableStateFlow<List<Article>>(emptyList())
            val observeArticlesUseCase: ObserveArticlesUseCase = mockk()
            every { observeArticlesUseCase() } returns cache

            val refreshArticlesUseCase: RefreshArticlesUseCase = mockk()
            coEvery { refreshArticlesUseCase(page = 1, perPage = 30) } returns AppResult.Success(Unit)

            val viewModel = ArticlesListViewModel(observeArticlesUseCase, refreshArticlesUseCase)
            advanceUntilIdle()
            // init{} already refreshed once; a resume before any navigation
            // happened (the screen's very first appearance) must not double it.
            viewModel.onScreenResumed()
            advanceUntilIdle()
            coVerify(exactly = 1) { refreshArticlesUseCase(page = 1, perPage = 30) }

            // A second resume simulates coming back from the detail screen.
            viewModel.onScreenResumed()
            advanceUntilIdle()
            coVerify(exactly = 2) { refreshArticlesUseCase(page = 1, perPage = 30) }
        }
}
