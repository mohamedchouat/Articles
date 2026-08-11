package com.chtmed.articles.presentation.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.chtmed.articles.presentation.detail.ArticleDetailScreen
import com.chtmed.articles.presentation.list.ArticlesListScreen

@Composable
fun ArticlesNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.ArticlesList.route
    ) {
        composable(route = Screen.ArticlesList.route) {
            ArticlesListScreen(
                onArticleClick = { articleId ->
                    navController.navigate(Screen.ArticleDetail.createRoute(articleId))
                }
            )
        }

        composable(
            route = Screen.ArticleDetail.route,
            arguments = listOf(
                navArgument(Screen.ArticleDetail.ARG_ARTICLE_ID) { type = NavType.IntType }
            ),
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
            exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
        ) {
            ArticleDetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
