package com.chtmed.articles.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response shape for GET https://dev.to/api/articles (list) and
 * GET https://dev.to/api/articles/{id} (detail — a superset of the same fields
 * plus body_html/body_markdown). Free, public, no API key required.
 *
 * All fields are nullable/defaulted where the API is known to omit them, so a
 * partial or slightly-changed payload never crashes deserialization.
 */
@Serializable
data class ArticleDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("cover_image") val coverImage: String? = null,
    @SerialName("social_image") val socialImage: String? = null,
    @SerialName("published_at") val publishedAt: String? = null,
    @SerialName("readable_publish_date") val readablePublishDate: String? = null,
    @SerialName("tag_list")
    @Serializable(with = FlexibleTagListSerializer::class)
    val tagList: List<String> = emptyList(),
    @SerialName("url") val url: String? = null,
    @SerialName("comments_count") val commentsCount: Int = 0,
    @SerialName("public_reactions_count") val publicReactionsCount: Int = 0,
    @SerialName("reading_time_minutes") val readingTimeMinutes: Int = 0,
    @SerialName("user") val user: UserDto? = null,
    @SerialName("body_html") val bodyHtml: String? = null
)
