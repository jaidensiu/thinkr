package com.example.thinkr.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.thinkr.ui.home.HomeScreen
import com.example.thinkr.ui.landing.LandingScreen
import com.example.thinkr.ui.theme.ThinkrTheme

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
                        startDestination = Route.Landing
                    ) {

                        composable<Route.Landing> {
                            LandingScreen(
                                onLogin = { navController.navigate(route = Route.Home) },
                                onSignUp = { navController.navigate(route = Route.Home) }
                            )
                        }

                        composable<Route.Home> {
                            HomeScreen()
                        }
                    }
                }
            }
        }
    }
}
