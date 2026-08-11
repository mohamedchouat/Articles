package com.chtmed.articles.di

import android.content.Context
import androidx.room.Room
import com.chtmed.articles.data.local.ArticleDao
import com.chtmed.articles.data.local.ArticlesDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideArticlesDatabase(@ApplicationContext context: Context): ArticlesDatabase =
        Room.databaseBuilder(context, ArticlesDatabase::class.java, "articles.db").build()

    @Provides
    @Singleton
    fun provideArticleDao(database: ArticlesDatabase): ArticleDao = database.articleDao()
}
