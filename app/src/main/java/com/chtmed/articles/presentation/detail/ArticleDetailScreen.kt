package com.chtmed.articles.presentation.detail

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.chtmed.articles.R
import com.chtmed.articles.domain.model.ArticleDetail
import com.chtmed.articles.presentation.components.ErrorView
import com.chtmed.articles.presentation.components.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    onBackClick: () -> Unit,
    viewModel: ArticleDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when {
            uiState.isLoading -> LoadingView(modifier = Modifier.padding(paddingValues))

            uiState.error != null && uiState.article == null -> ErrorView(
                error = uiState.error!!,
                onRetry = { viewModel.onEvent(ArticleDetailEvent.Retry) },
                modifier = Modifier.padding(paddingValues)
            )

            uiState.article != null -> ArticleDetailContent(
                article = uiState.article!!,
                onBackClick = onBackClick
            )
        }

        if (uiState.article == null) {
            BackButton(onClick = onBackClick, tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun ArticleDetailContent(
    article: ArticleDetail,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ArticleHero(article = article, onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            Row(
                modifier = Modifier.padding(top = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                article.authorAvatarUrl?.let { avatarUrl ->
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = article.authorName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.by_author, article.authorName),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = article.publishedAt,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ArticleStats(article = article)
            }

            if (article.tags.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(items = article.tags) { tag ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text("#$tag") },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = null
                        )
                    }
                }
            }

            if (article.bodyHtml.isNotBlank()) {
                ArticleHtmlBody(
                    html = article.bodyHtml,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                )
            } else if (article.description.isNotBlank()) {
                Text(
                    text = article.description,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
        }
    }
}

@Composable
private fun ArticleHero(article: ArticleDetail, onBackClick: () -> Unit) {
    Box {
        if (article.coverImageUrl != null) {
            AsyncImage(
                model = article.coverImageUrl,
                contentDescription = article.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.35f),
                                Color.Transparent,
                                Color.Transparent
                            ),
                            endY = 260f
                        )
                    )
            )
        }
        BackButton(
            onClick = onBackClick,
            tint = if (article.coverImageUrl != null) Color.White else MaterialTheme.colorScheme.onSurface,
            containerColor = if (article.coverImageUrl != null) {
                Color.Black.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    }
}

@Composable
private fun BackButton(
    onClick: () -> Unit,
    tint: Color,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .statusBarsPadding()
            .padding(12.dp),
        colors = IconButtonDefaults.iconButtonColors(containerColor = containerColor)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(id = R.string.back),
            tint = tint
        )
    }
}

@Composable
private fun ArticleStats(article: ArticleDetail) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(icon = Icons.Filled.Schedule, value = article.readingTimeMinutes)
            StatItem(icon = Icons.Filled.FavoriteBorder, value = article.reactionsCount)
            StatItem(icon = Icons.Filled.ChatBubbleOutline, value = article.commentsCount)
        }
    }
}

@Composable
private fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * DEV.to returns article bodies as sanitized HTML (body_html). Rendering it in
 * a lightweight WebView is far more faithful than stripping tags to plain
 * text, while remaining simple and framework-idiomatic via AndroidView.
 */
@Composable
private fun ArticleHtmlBody(html: String, modifier: Modifier = Modifier) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val linkColor = MaterialTheme.colorScheme.primary

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = false
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        update = { webView ->
            val textColorHex = String.format("#%06X", 0xFFFFFF and textColor.toArgb())
            val linkColorHex = String.format("#%06X", 0xFFFFFF and linkColor.toArgb())
            val styledHtml = """
                <html>
                <head>
                <style>
                  body { color: $textColorHex; font-family: sans-serif; font-size: 16px; line-height: 1.6; }
                  a { color: $linkColorHex; }
                  img { max-width: 100%; height: auto; border-radius: 12px; }
                  pre { white-space: pre-wrap; background: rgba(128,128,128,0.15); padding: 12px; border-radius: 12px; }
                  code { background: rgba(128,128,128,0.15); padding: 2px 4px; border-radius: 4px; }
                </style>
                </head>
                <body>$html</body>
                </html>
            """.trimIndent()
            webView.loadDataWithBaseURL(null, styledHtml, "text/html", "UTF-8", null)
        }
    )
}
