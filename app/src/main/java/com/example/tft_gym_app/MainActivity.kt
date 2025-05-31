package com.example.tft_gym_app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tft_gym_app.ui.theme.TFTGymAppTheme
import com.google.firebase.FirebaseApp
import firebase.auth.AuthRepository
import pages.GetLoginScreen
import pages.GetRegisterScreen
import pages.GetUserDataScreen
import routes.NavigationActions
import routes.Routes
import uiPrincipal.MyComposeApp
import viewModel.api.GymViewModel
import viewModel.rm.RmCalculator

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
                val viewModelRmCalculator: RmCalculator = viewModel()
                val viewModelRepository : AuthRepository = viewModel()
                val selectedText by gymViewModel.observeSelectedText.observeAsState()
                val weight by gymViewModel.observeWeight.observeAsState()
                val rm by viewModelRmCalculator.estimatedRm.observeAsState()
                val currentDateTime = java.time.LocalDate.now()
                LaunchedEffect(Unit) {
                    gymViewModel.resultResponse.collect { result ->
                        result?.let {
                            Log.d("MainActivity", "resultados: $result")
                            Log.d("MainActivity", "Datos -> selectedText: $selectedText , weight: $weight , rm: $rm")
                            try {
                                viewModelRmCalculator.analyzeRepetition(
                                    weight = weight?.toFloat() ?: 0f,
                                    reps = result.results.reps
                                )

                                viewModelRepository.editUserFromVideo(
                                    exercise = selectedText.toString(),
                                    weight = weight?.toFloat() ?: 0f,
                                    repetitions = result.results.reps,
                                    date = currentDateTime.toString(),
                                    rm = rm
                                )
                                gymViewModel.actualizarWeight("")
                                gymViewModel.actualizarSelectedText("")
                                gymViewModel.clearResponses()

                            } catch (e: Exception) {
                                Log.e("VideoPage", "Error al guardar en Firebase: ${e.message}")
                            }
                        }
                    }
                }
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
