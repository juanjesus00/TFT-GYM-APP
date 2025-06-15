package uiPrincipal

import android.annotation.SuppressLint
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import pages.GetEditUserInfo
import pages.GetHistorialPage
import pages.GetHistory
import pages.GetRoutinePage
import pages.GetRoutineSelector
import pages.GetVideoPage
import pages.RutinaGeneradorScreen
import viewModel.api.GymViewModel

object LanguageManager {
    var languageCode by mutableStateOf("")
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MyComposeApp(
    navigationActions: NavigationActions,
    navController: NavController,
    gymViewModel: GymViewModel
){
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val color = Color(0xFF2A2C38)
    val scrollState = rememberScrollState()
    var showBars by remember { mutableStateOf(true) }
    var previousScrollOffset by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val systemUiController = rememberSystemUiController()

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