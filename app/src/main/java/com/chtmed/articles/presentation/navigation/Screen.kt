package com.chtmed.articles.presentation.navigation

/**
 * Centralized, typed navigation destinations. Keeping route strings in one
 * place avoids typos scattered across composables.
 */
sealed class Screen(val route: String) {
    data object ArticlesList : Screen("articles_list")

    data object ArticleDetail : Screen("article_detail/{articleId}") {
        const val ARG_ARTICLE_ID = "articleId"
        fun createRoute(articleId: Int) = "article_detail/$articleId"
    }
}
