package com.chtmed.articles.domain.model

/**
 * Pure domain model for an article summary, as shown in the list screen.
 * Contains no Android framework or serialization dependencies, per Clean Architecture.
 */
data class Article(
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
    val reactionsCount: Int
)
