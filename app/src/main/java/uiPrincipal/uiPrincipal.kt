package uiPrincipal

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import components.GetHeader
import components.GetNavigatorBar
import kotlinx.coroutines.launch
import pages.GetPrincipalMidSection
import pages.GetUserProfile
import routes.NavigationActions
import routes.Routes
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import firebase.auth.AuthRepository
import pages.GetEditUserInfo
import pages.GetHistorialPage
import pages.GetHistory
import pages.GetRoutinePage
import pages.GetRoutineSelector
import pages.GetVideoPage
import pages.RutinaGeneradorScreen
import viewModel.api.GymViewModel
import viewModel.rm.RmCalculator

object LanguageManager {
    var languageCode by mutableStateOf("")
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MyComposeApp(
    navigationActions: NavigationActions,
    navController: NavController,
    gymViewModel: GymViewModel,
    viewModelRepository: AuthRepository,
    viewModelRmCalculator: RmCalculator

){
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val color = Color(0xFF2A2C38)
    val scrollState = rememberScrollState()
    var showBars by remember { mutableStateOf(true) }
    var previousScrollOffset by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val systemUiController = rememberSystemUiController()
    var listExercise by remember { mutableStateOf(emptyMap<String, Float>()) }
    val maxRmMap by viewModelRepository.maxRm.collectAsState()
    var bodyWeight by remember { mutableFloatStateOf(0f) }
    var levelStrength by remember {mutableStateOf("")}
    val exerciseList = listOf("Press de Banca", "Peso Muerto", "Sentadilla")

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        systemUiController.setStatusBarColor(
            color = color,
            darkIcons = false // Cambia a true si usas íconos oscuros
        )
        systemUiController.setNavigationBarColor(
            color = color,
            darkIcons = false, // Cambia a true si usas íconos oscuros
            navigationBarContrastEnforced = false
        )
        viewModelRepository.getInfoUser { user ->
            user?.let {
                bodyWeight = it["weight"]?.toString()?.toFloatOrNull() ?: 0f
                Log.d("Peso corporal", "$bodyWeight")

                viewModelRepository.getRMUser { rmMap ->
                    listExercise = rmMap

                    exerciseList.forEach { exercise ->
                        val currentRm = rmMap[exercise] ?: 0f
                        val previousRm = maxRmMap[exercise] ?: 0f

                        // Si el valor anterior es 0 (o no existe), asumimos que aún no se ha inicializado: no hacemos nada
                        if (previousRm == 0f || currentRm == 0f) {
                            Log.d("Max RM", "$exercise - Se omite comparación por valor inicial 0 (actual: $currentRm, anterior: $previousRm)")
                            return@forEach
                        }

                        Log.d("Max RM", "$exercise - Actual: $currentRm, Anterior: $previousRm")

                        if (currentRm != previousRm) {
                            viewModelRmCalculator.getLevelStrength(
                                exercise = exercise,
                                bodyWeight = bodyWeight,
                                rm = currentRm
                            ) { level ->
                                levelStrength = level
                                viewModelRepository.observeMaxRmChanges(
                                    exercise = exercise,
                                    bodyWeight = bodyWeight,
                                    context = context,
                                    levelStrength = level
                                )
                            }

                            viewModelRepository.setNewMaxRm(exercise, currentRm)
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(scrollState.value) {
        coroutineScope.launch {
            val currentOffset = scrollState.value

            // Mostrar/ocultar barras basado en la dirección del scroll
            showBars = if (currentOffset > previousScrollOffset) {
                false // Ocultar cuando el usuario scrollea hacia arriba
            } else {
                true // Mostrar cuando el usuario scrollea hacia abajo
            }
            previousScrollOffset = currentOffset
        }
    }

    Scaffold (
        topBar = {
            AnimatedVisibility(visible = showBars) {
                GetHeader(navigationActions, navController)
            }
        },
        bottomBar = {
            AnimatedVisibility(visible = showBars) {
                Box(
                    modifier = Modifier.offset(y = (-10).dp)
                ) {
                    GetNavigatorBar(navigationActions, navController)
                }

            }
        }

    ){
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2A2C38))
            .navigationBarsPadding() // ← ESTO
        ) {
            when(currentRoute) {
                Routes.HOME -> GetPrincipalMidSection(scrollState, navigationActions, navController, gymViewModel)
                Routes.USERPROFILE -> GetUserProfile(scrollState, navigationActions, navController,gymViewModel)
                Routes.VIDEO -> GetVideoPage(scrollState, navigationActions, navController,gymViewModel)
                Routes.EDITUSERINFO -> GetEditUserInfo(scrollState, navigationActions, navController,gymViewModel)
                Routes.HISTORY -> GetHistory(scrollState, navigationActions, navController,gymViewModel)
                Routes.HISTORYPAGE -> GetHistorialPage(scrollState, navigationActions, navController,gymViewModel)
                Routes.ROUTINEGENERATOR -> RutinaGeneradorScreen(scrollState, navigationActions, navController,gymViewModel)
                Routes.ROUTINESELECTOR -> GetRoutineSelector(scrollState, navigationActions, navController,gymViewModel)
                Routes.ROUTINEPAGE -> GetRoutinePage(scrollState, navigationActions, navController,gymViewModel)
            }
        }
    }
}