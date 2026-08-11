package com.chtmed.articles.di

import com.chtmed.articles.data.repository.ArticleRepositoryImpl
import com.chtmed.articles.domain.repository.ArticleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the domain-facing ArticleRepository interface to its data-layer
 * implementation. Using @Binds (over @Provides) is preferred here since it's
 * a simple interface-to-impl binding — generates less code and is faster to
 * process at compile time.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindArticleRepository(
        impl: ArticleRepositoryImpl
    ): ArticleRepository
}
