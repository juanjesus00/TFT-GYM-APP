package uiPrincipal

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import components.GetHeader
import components.GetNavigatorBar
import kotlinx.coroutines.launch
import pages.GetPrincipalMidSection
import routes.NavigationActions
import routes.Routes


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MyComposeApp(navigationActions: NavigationActions, navController: NavController){
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val color = Color(0xFF2A2C38)
    val scrollState = rememberScrollState()
    var showBars by remember { mutableStateOf(true) }
    var previousScrollOffset by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

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
                GetNavigatorBar(navigationActions, navController)
            }
        }

    ){
        if(currentRoute == Routes.HOME){
            GetPrincipalMidSection(scrollState, navigationActions, navController)
        }
    }
}