package com.chtmed.restapidebugger.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chtmed.restapidebugger.ui.theme.RestApiDebuggerTheme

/**
 * Standalone entry point for the debugger UI. Deliberately a plain
 * ComponentActivity (not @AndroidEntryPoint or otherwise tied to the host
 * app's DI graph) so this module works the same whether or not the host app
 * uses Hilt.
 */
class RestApiDebuggerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RestApiDebuggerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = ROUTE_HISTORY) {
                        composable(ROUTE_HISTORY) {
                            HistoryScreen(
                                onCallClick = { id -> navController.navigate("$ROUTE_DETAIL/$id") }
                            )
                        }
                        composable(
                            route = "$ROUTE_DETAIL/{$ARG_CALL_ID}",
                            arguments = listOf(navArgument(ARG_CALL_ID) { type = NavType.StringType })
                        ) { backStackEntry ->
                            val callId = backStackEntry.arguments?.getString(ARG_CALL_ID).orEmpty()
                            DetailScreen(
                                callId = callId,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val ROUTE_HISTORY = "history"
        private const val ROUTE_DETAIL = "detail"
        private const val ARG_CALL_ID = "callId"

        fun newIntent(context: Context): Intent =
            Intent(context, RestApiDebuggerActivity::class.java)
    }
}
