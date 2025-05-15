package com.example.tft_gym_app

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tft_gym_app.ui.theme.TFTGymAppTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import pages.GetLoginScreen
import pages.GetRegisterScreen
import pages.GetUserDataScreen
import routes.NavigationActions
import routes.Routes
import uiPrincipal.MyComposeApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        setContent {
            TFTGymAppTheme {
                val navController = rememberNavController()
                val navigationActions = NavigationActions(navController)
                NavHost(navController = navController, startDestination = Routes.HOME){
                    composable(Routes.HOME){ MyComposeApp(navigationActions, navController) }
                    composable(Routes.LOGIN){ GetLoginScreen(navigationActions, navController) }
                    composable(Routes.REGISTER){ GetRegisterScreen(navigationActions, navController) }
                    composable(Routes.USERDATA){ GetUserDataScreen(navigationActions, navController) }
                    composable(Routes.USERPROFILE){ MyComposeApp(navigationActions, navController) }
                    composable(Routes.VIDEO){ MyComposeApp(navigationActions, navController) }
                }

            }
        }
    }
}
