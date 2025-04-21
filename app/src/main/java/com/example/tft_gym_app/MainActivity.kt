package com.example.tft_gym_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
