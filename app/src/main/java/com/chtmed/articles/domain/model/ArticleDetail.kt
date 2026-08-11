package com.chtmed.articles.domain.model

/**
 * Pure domain model for the full article, including its rendered HTML body.
 * Extends the summary fields the list already has so the detail screen can
 * render instantly while the full body loads.
 */
data class ArticleDetail(
    val id: Int,
    val title: String,
    val description: String,
    val coverImageUrl: String?,
    val authorName: String,
    val authorAvatarUrl: String?,
    val publishedAt: String,
    val tags: List<String>,
    val readingTimeMinutes: Int,
    val commentsCount: Int,
    val reactionsCount: Int,
    val bodyHtml: String,
    val canonicalUrl: String
)
