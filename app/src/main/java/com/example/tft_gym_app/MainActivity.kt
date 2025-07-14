package com.example.tft_gym_app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
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
import android.Manifest.permission.POST_NOTIFICATIONS
import android.R.attr.id
import android.app.PendingIntent
import android.content.Intent
import android.os.Environment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(POST_NOTIFICATIONS),
                    1001
                )
            }
        }
        FirebaseApp.initializeApp(this)
        createNotificationChannel()
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
                var bodyWeight by remember { mutableFloatStateOf(0f) }

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
                                    rm = rm,
                                    onSuccess = {showSuccessNotification(this@MainActivity)} // een este caso siempre redirige a Home, pero deberia de enviar una notificacion al usuario
                                )
                                gymViewModel.actualizarWeight("")
                                gymViewModel.actualizarSelectedText("")


                                gymViewModel.downloadProcessedVideo(
                                    context = this@MainActivity,
                                    analysisId = gymViewModel.analyzeResponse.value,
                                    onSuccess = {
                                        Log.d("MainActivity", "Video descargado correctamente.")
                                        showDownloadNotification(context = this@MainActivity)
                                         // Puedes mostrar una notificación aquí
                                    },
                                    onError = { e ->
                                        Log.e("MainActivity", "Error al descargar el video: ${e.message}")
                                    }
                                )

                                gymViewModel.clearResponses()

                            } catch (e: Exception) {
                                Log.e("VideoPage", "Error al guardar en Firebase: ${e.message}")
                            }
                        }
                    }

                    viewModelRepository.getInfoUser { user ->
                        user?.let {
                            bodyWeight = it["weight"]?.toString()?.toFloatOrNull() ?: 0f
                        }
                    }


                }

                NavHost(navController = navController, startDestination = Routes.HOME){
                    composable(Routes.HOME){ MyComposeApp(navigationActions, navController,gymViewModel, viewModelRepository, viewModelRmCalculator) }
                    composable(Routes.LOGIN){ GetLoginScreen(navigationActions, navController,gymViewModel) }
                    composable(Routes.REGISTER){ GetRegisterScreen(navigationActions, navController,gymViewModel) }
                    composable(Routes.USERDATA){ GetUserDataScreen(navigationActions, navController,gymViewModel) }
                    composable(Routes.USERPROFILE){ MyComposeApp(
                        navigationActions,
                        navController,
                        gymViewModel,
                        viewModelRepository,
                        viewModelRmCalculator
                    ) }
                    composable(Routes.VIDEO){ MyComposeApp(
                        navigationActions,
                        navController,
                        gymViewModel,
                        viewModelRepository,
                        viewModelRmCalculator
                    ) }
                    composable(Routes.EDITUSERINFO){ MyComposeApp(
                        navigationActions,
                        navController,
                        gymViewModel,
                        viewModelRepository,
                        viewModelRmCalculator
                    ) }
                    composable(Routes.HISTORY){ MyComposeApp(
                        navigationActions,
                        navController,
                        gymViewModel,
                        viewModelRepository,
                        viewModelRmCalculator
                    ) }
                    composable(Routes.HISTORYPAGE){ MyComposeApp(
                        navigationActions,
                        navController,
                        gymViewModel,
                        viewModelRepository,
                        viewModelRmCalculator
                    ) }
                    composable(Routes.ROUTINEGENERATOR){ MyComposeApp(
                        navigationActions,
                        navController,
                        gymViewModel,
                        viewModelRepository,
                        viewModelRmCalculator
                    ) }
                    composable(Routes.ROUTINESELECTOR){ MyComposeApp(
                        navigationActions,
                        navController,
                        gymViewModel,
                        viewModelRepository,
                        viewModelRmCalculator
                    ) }
                    composable(Routes.ROUTINEPAGE){ MyComposeApp(
                        navigationActions,
                        navController,
                        gymViewModel,
                        viewModelRepository,
                        viewModelRmCalculator
                    ) }
                }

            }
        }
        createNotificationChannel(this@MainActivity)


    }
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "download_channel"
            val channelName = "Descargas de videos"
            val channelDescription = "Notificaciones para descargas completadas"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Datos guardados"
            val descriptionText = "Notifica cuando los datos del usuario han sido guardados"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("firebase_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun showSuccessNotification(context: Context) {
        val builder = NotificationCompat.Builder(this, "firebase_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Usa tu icono aquí
            .setContentTitle("AI análisis")
            .setContentText("El video se ha analizado correctemente")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            ) {
                with(NotificationManagerCompat.from(context)) {
                    notify(1001, builder.build())
                }
            } else {
                Log.w("Notificación", "Permiso POST_NOTIFICATIONS denegado. No se puede mostrar la notificación.")
            }
        }
    }

    private fun showDownloadNotification(context: Context) {


        val builder = NotificationCompat.Builder(context, "download_channel")
            .setSmallIcon(R.drawable.video)
            .setContentTitle ("Video descargado")
            .setContentText("El video ha sido descargado correctamente")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(context)) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            ) {
                with(NotificationManagerCompat.from(context)) {
                    notify(1001, builder.build())
                }
            } else {
                Log.w("Notificación", "Permiso POST_NOTIFICATIONS denegado. No se puede mostrar la notificación.")
            }
        }
    }
}
