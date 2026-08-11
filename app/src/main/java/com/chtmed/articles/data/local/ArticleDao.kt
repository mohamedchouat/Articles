package com.chtmed.articles.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    /**
     * Room's single source of truth for the list screen: this Flow is
     * automatically re-emitted whenever any write touches the `articles`
     * table, from either a list refresh or a detail fetch.
     */
    @Query("SELECT * FROM articles ORDER BY listOrder ASC")
    fun observeArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE id = :id LIMIT 1")
    suspend fun getArticleById(id: Int): ArticleEntity?

    /**
     * Upserts a single row, preserving any previously cached bodyHtml/canonicalUrl
     * when the incoming values are null (i.e. this write came from the list
     * endpoint, not the detail endpoint).
     */
    @Query(
        """
        INSERT INTO articles
            (id, title, description, coverImageUrl, authorName, authorAvatarUrl,
             publishedAt, tags, readingTimeMinutes, commentsCount, reactionsCount,
             listOrder, bodyHtml, canonicalUrl)
        VALUES
            (:id, :title, :description, :coverImageUrl, :authorName, :authorAvatarUrl,
             :publishedAt, :tags, :readingTimeMinutes, :commentsCount, :reactionsCount,
             :listOrder, :bodyHtml, :canonicalUrl)
        ON CONFLICT(id) DO UPDATE SET
            title = excluded.title,
            description = excluded.description,
            coverImageUrl = excluded.coverImageUrl,
            authorName = excluded.authorName,
            authorAvatarUrl = excluded.authorAvatarUrl,
            publishedAt = excluded.publishedAt,
            tags = excluded.tags,
            readingTimeMinutes = excluded.readingTimeMinutes,
            commentsCount = excluded.commentsCount,
            reactionsCount = excluded.reactionsCount,
            listOrder = excluded.listOrder,
            bodyHtml = COALESCE(excluded.bodyHtml, articles.bodyHtml),
            canonicalUrl = COALESCE(excluded.canonicalUrl, articles.canonicalUrl)
        """
    )
    suspend fun upsertArticle(
        id: Int,
        title: String,
        description: String,
        coverImageUrl: String?,
        authorName: String,
        authorAvatarUrl: String?,
        publishedAt: String,
        tags: String,
        readingTimeMinutes: Int,
        commentsCount: Int,
        reactionsCount: Int,
        listOrder: Int,
        bodyHtml: String?,
        canonicalUrl: String?
    )

    @Transaction
    suspend fun upsertArticles(entities: List<ArticleEntity>) {
        entities.forEach { upsertArticle(it) }
    }

    suspend fun upsertArticle(entity: ArticleEntity) {
        upsertArticle(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            coverImageUrl = entity.coverImageUrl,
            authorName = entity.authorName,
            authorAvatarUrl = entity.authorAvatarUrl,
            publishedAt = entity.publishedAt,
            tags = Converters.fromTagList(entity.tags),
            readingTimeMinutes = entity.readingTimeMinutes,
            commentsCount = entity.commentsCount,
            reactionsCount = entity.reactionsCount,
            listOrder = entity.listOrder,
            bodyHtml = entity.bodyHtml,
            canonicalUrl = entity.canonicalUrl
        )
    }
}
