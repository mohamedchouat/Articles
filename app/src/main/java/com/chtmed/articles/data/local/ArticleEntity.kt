package com.chtmed.articles.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local cache of an article. Doubles as both the list-summary row and the
 * detail row: bodyHtml/canonicalUrl are null until the detail endpoint has
 * been fetched at least once, so ArticleDao's upsert can merge a fresh list
 * snapshot into a row without wiping a previously cached article body.
 */
@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: Int,
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
    val listOrder: Int,
    val bodyHtml: String?,
    val canonicalUrl: String?
)
