package com.chtmed.articles.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.chtmed.articles.R
import com.chtmed.articles.core.util.AppError
import com.chtmed.articles.domain.model.Article
import com.chtmed.articles.presentation.components.ErrorView
import com.chtmed.articles.presentation.components.LoadingView
import com.chtmed.articles.presentation.list.components.ArticleListItem
import com.chtmed.articles.presentation.theme.ArticlesAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesListScreen(
    onArticleClick: (Int) -> Unit,
    viewModel: ArticlesListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Returning from the detail screen doesn't naturally trigger a reload —
    // this screen's composition (and any locally `remember`ed state) is torn
    // down while the detail screen is on top and rebuilt when you come back,
    // so the "skip the first resume" bookkeeping has to live on the
    // ViewModel (see onScreenResumed()), not here, to survive that round trip.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onScreenResumed()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(id = R.string.articles_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = stringResource(id = R.string.articles_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        ArticlesListContent(
            uiState = uiState,
            onArticleClick = onArticleClick,
            onRetry = { viewModel.onEvent(ArticlesListEvent.Retry) },
            onRefresh = { viewModel.onEvent(ArticlesListEvent.Refresh) },
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticlesListContent(
    uiState: ArticlesListUiState,
    onArticleClick: (Int) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        when {
            uiState.isLoading -> LoadingView(modifier = Modifier.fillMaxSize())

            uiState.error != null && uiState.articles.isEmpty() -> ErrorView(
                error = uiState.error,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize()
            )

            uiState.isEmpty -> EmptyArticlesView(modifier = Modifier.fillMaxSize())

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(items = uiState.articles, key = { it.id }) { article ->
                    ArticleListItem(
                        article = article,
                        onClick = { onArticleClick(article.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyArticlesView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.empty_articles),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticlesListContentPreview() {
    ArticlesAppTheme {
        ArticlesListContent(
            uiState = ArticlesListUiState(
                articles = listOf(
                    Article(
                        id = 1,
                        title = "Understanding Clean Architecture on Android",
                        description = "A practical guide to structuring scalable apps.",
                        coverImageUrl = null,
                        authorName = "Jane Doe",
                        authorAvatarUrl = null,
                        publishedAt = "Aug 9",
                        tags = listOf("android", "kotlin"),
                        readingTimeMinutes = 6,
                        commentsCount = 12,
                        reactionsCount = 84
                    )
                )
            ),
            onArticleClick = {},
            onRetry = {},
            onRefresh = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticlesListErrorPreview() {
    ArticlesAppTheme {
        ArticlesListContent(
            uiState = ArticlesListUiState(error = AppError.NoInternet),
            onArticleClick = {},
            onRetry = {},
            onRefresh = {}
        )
    }
}
