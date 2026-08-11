package com.chtmed.articles.data.mapper

import com.chtmed.articles.data.local.ArticleEntity
import com.chtmed.articles.data.remote.dto.ArticleDto
import com.chtmed.articles.domain.model.Article
import com.chtmed.articles.domain.model.ArticleDetail

/**
 * Data layer is the only place allowed to know about DTOs, so all DTO -> domain
 * conversion is centralized here. Keeping this separate from the repository
 * makes both independently and trivially unit-testable.
 */
fun ArticleDto.toDomain(): Article = Article(
    id = id,
    title = title.orEmpty(),
    description = description.orEmpty(),
    coverImageUrl = coverImage ?: socialImage,
    authorName = user?.name ?: user?.username ?: "Unknown author",
    authorAvatarUrl = user?.profileImage90 ?: user?.profileImage,
    publishedAt = readablePublishDate ?: publishedAt.orEmpty(),
    tags = tagList,
    readingTimeMinutes = readingTimeMinutes,
    commentsCount = commentsCount,
    reactionsCount = publicReactionsCount
)

fun ArticleDto.toDetailDomain(): ArticleDetail = ArticleDetail(
    id = id,
    title = title.orEmpty(),
    description = description.orEmpty(),
    coverImageUrl = coverImage ?: socialImage,
    authorName = user?.name ?: user?.username ?: "Unknown author",
    authorAvatarUrl = user?.profileImage90 ?: user?.profileImage,
    publishedAt = readablePublishDate ?: publishedAt.orEmpty(),
    tags = tagList,
    readingTimeMinutes = readingTimeMinutes,
    commentsCount = commentsCount,
    reactionsCount = publicReactionsCount,
    bodyHtml = bodyHtml.orEmpty(),
    canonicalUrl = url.orEmpty()
)

/** Caches a list-summary row; bodyHtml/canonicalUrl stay null so a previously cached detail isn't wiped. */
fun Article.toEntity(listOrder: Int): ArticleEntity = ArticleEntity(
    id = id,
    title = title,
    description = description,
    coverImageUrl = coverImageUrl,
    authorName = authorName,
    authorAvatarUrl = authorAvatarUrl,
    publishedAt = publishedAt,
    tags = tags,
    readingTimeMinutes = readingTimeMinutes,
    commentsCount = commentsCount,
    reactionsCount = reactionsCount,
    listOrder = listOrder,
    bodyHtml = null,
    canonicalUrl = null
)

/** Caches a fully-fetched detail row, including the article body. */
fun ArticleDetail.toEntity(listOrder: Int): ArticleEntity = ArticleEntity(
    id = id,
    title = title,
    description = description,
    coverImageUrl = coverImageUrl,
    authorName = authorName,
    authorAvatarUrl = authorAvatarUrl,
    publishedAt = publishedAt,
    tags = tags,
    readingTimeMinutes = readingTimeMinutes,
    commentsCount = commentsCount,
    reactionsCount = reactionsCount,
    listOrder = listOrder,
    bodyHtml = bodyHtml,
    canonicalUrl = canonicalUrl
)

fun ArticleEntity.toDomain(): Article = Article(
    id = id,
    title = title,
    description = description,
    coverImageUrl = coverImageUrl,
    authorName = authorName,
    authorAvatarUrl = authorAvatarUrl,
    publishedAt = publishedAt,
    tags = tags,
    readingTimeMinutes = readingTimeMinutes,
    commentsCount = commentsCount,
    reactionsCount = reactionsCount
)

fun ArticleEntity.toDetailDomain(): ArticleDetail = ArticleDetail(
    id = id,
    title = title,
    description = description,
    coverImageUrl = coverImageUrl,
    authorName = authorName,
    authorAvatarUrl = authorAvatarUrl,
    publishedAt = publishedAt,
    tags = tags,
    readingTimeMinutes = readingTimeMinutes,
    commentsCount = commentsCount,
    reactionsCount = reactionsCount,
    bodyHtml = bodyHtml.orEmpty(),
    canonicalUrl = canonicalUrl.orEmpty()
)
