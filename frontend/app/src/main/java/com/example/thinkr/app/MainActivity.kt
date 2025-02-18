package com.example.thinkr.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.thinkr.domain.model.DocumentItem
import com.example.thinkr.ui.home.HomeScreen
import com.example.thinkr.ui.document_options.DocumentOptionsScreen
import com.example.thinkr.ui.landing.LandingScreen
import com.example.thinkr.ui.landing.LandingScreenViewModel
import com.example.thinkr.ui.theme.ThinkrTheme
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            ThinkrTheme {
                Column(modifier = Modifier.padding(start = 24.dp, top = 48.dp, end = 24.dp)) {
                    NavHost(
                        navController = navController,
                        startDestination = Route.Landing // TODO: add a check here if authed and cache it in sqlite
                    ) {
                        composable<Route.Landing> {
                            val viewModel = koinViewModel<LandingScreenViewModel>()

                            LandingScreen(
                                viewModel = viewModel,
                                onLogin = { navController.navigate(route = Route.Home) },
                                onSignUp = { navController.navigate(route = Route.Home) }
                            )
                        }

                        composable<Route.Home> {
                            HomeScreen(navController = navController)
                        }

                        composable(
                            route = Route.DocumentOptions.ROUTE,
                            arguments = listOf(navArgument("documentJson") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val json = backStackEntry.arguments?.getString("documentJson") ?: ""
                            val document = Json.decodeFromString<DocumentItem>(Uri.decode(json)) // Decode JSON back to object
                            DocumentOptionsScreen(document)
                        }
                    }
                }
            }
        }
    }
}
