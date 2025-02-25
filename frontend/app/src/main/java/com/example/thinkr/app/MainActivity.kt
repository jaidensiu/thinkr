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
import com.example.thinkr.ui.document_details.DocumentDetailsScreen
import androidx.navigation.navigation
import com.example.thinkr.ui.home.HomeScreen
import com.example.thinkr.ui.document_options.DocumentOptionsScreen
import com.example.thinkr.ui.landing.LandingScreen
import com.example.thinkr.ui.landing.LandingScreenViewModel
import com.example.thinkr.ui.payment.PaymentScreen
import com.example.thinkr.ui.payment.PaymentViewModel
import com.example.thinkr.ui.profile.ProfileScreen
import com.example.thinkr.ui.profile.ProfileViewModel
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
                        startDestination = Route.RouteGraph
                    ) {
                        // TODO: add a check here if authed and cache it in sqlite
                        navigation<Route.RouteGraph>(startDestination = Route.Landing) {
                            composable<Route.Landing> {
                                val viewModel = koinViewModel<LandingScreenViewModel>()

                                LandingScreen(
                                    viewModel = viewModel,
                                    onLogin = { navController.navigate(Route.Home) },
                                    onSignUp = { navController.navigate(Route.Home) }
                                )
                            }

                            composable<Route.Home> {
                                HomeScreen(navController = navController)
                            }

                            composable(
                                route = Route.DocumentOptions.ROUTE,
                                arguments = listOf(navArgument(Route.DocumentOptions.ARGUMENT) {
                                    type = NavType.StringType
                                })
                            ) { backStackEntry ->
                                val json =
                                    backStackEntry.arguments?.getString(Route.DocumentOptions.ARGUMENT)
                                        ?: ""
                                val document =
                                    Json.decodeFromString<DocumentItem>(Uri.decode(json)) // Decode JSON back to object
                                DocumentOptionsScreen(document)
                            }

                            composable(
                                route = Route.DocumentDetails.ROUTE,
                                arguments = listOf(navArgument(Route.DocumentDetails.ARGUMENT) {
                                    type = NavType.StringType
                                })
                            ) { backStackEntry ->
                                val json =
                                    backStackEntry.arguments?.getString(Route.DocumentDetails.ARGUMENT)
                                        ?: ""
                                val selectedUri = Uri.parse(Uri.decode(json))
                                DocumentDetailsScreen(navController, selectedUri)
                            }

                            composable<Route.Profile> {
                                val viewModel = koinViewModel<ProfileViewModel>()

                                ProfileScreen(
                                    viewModel = viewModel,
                                    onPressBack = { navController.navigate(Route.Home) },
                                    onSelectPremium = { navController.navigate(Route.Payment) }
                                )
                            }

                            composable<Route.Payment> {
                                val viewModel = koinViewModel<PaymentViewModel>()

                                PaymentScreen(
                                    viewModel = viewModel,
                                    onConfirm = { navController.navigate(Route.Profile) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
