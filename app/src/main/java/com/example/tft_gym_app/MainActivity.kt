package com.example.tft_gym_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tft_gym_app.ui.theme.TFTGymAppTheme
import routes.NavigationActions
import routes.Routes
import uiPrincipal.MyComposeApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TFTGymAppTheme {
                val navController = rememberNavController()
                val navigationActions = NavigationActions(navController)

                NavHost(navController = navController, startDestination = Routes.HOME){
                    composable(Routes.HOME){ MyComposeApp(navigationActions, navController) }
                }

            }
        }
    }
}
