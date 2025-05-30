package com.example.tft_gym_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tft_gym_app.ui.theme.TFTGymAppTheme
import com.google.firebase.FirebaseApp
import pages.GetLoginScreen
import pages.GetRegisterScreen
import pages.GetUserDataScreen
import routes.NavigationActions
import routes.Routes
import uiPrincipal.MyComposeApp
import viewModel.api.GymViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        setContent {
            TFTGymAppTheme {
                val navController = rememberNavController()
                val navigationActions = NavigationActions(navController)
                val gymViewModel: GymViewModel = viewModel()

                NavHost(navController = navController, startDestination = Routes.HOME){
                    composable(Routes.HOME){ MyComposeApp(navigationActions, navController,gymViewModel) }
                    composable(Routes.LOGIN){ GetLoginScreen(navigationActions, navController,gymViewModel) }
                    composable(Routes.REGISTER){ GetRegisterScreen(navigationActions, navController,gymViewModel) }
                    composable(Routes.USERDATA){ GetUserDataScreen(navigationActions, navController,gymViewModel) }
                    composable(Routes.USERPROFILE){ MyComposeApp(navigationActions, navController,gymViewModel) }
                    composable(Routes.VIDEO){ MyComposeApp(navigationActions, navController,gymViewModel) }
                    composable(Routes.EDITUSERINFO){ MyComposeApp(navigationActions, navController,gymViewModel) }
                }

            }
        }
    }
}
