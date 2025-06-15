package pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tft_gym_app.R
import components.box.GetHistoryExerciseBox
import components.box.GetRoutineTypeBox
import firebase.auth.AuthRepository
import routes.NavigationActions
import viewModel.api.GymViewModel

@Composable
fun GetRoutineSelector(
    scrollState: ScrollState,
    navigationActions: NavigationActions,
    navController: NavController,
    gymViewModel: GymViewModel,
    authRepository: AuthRepository = viewModel()
) {
    val routineList = listOf(
        Triple("strength", R.drawable.fuerza, 0),
        Triple("hypertrophy", R.drawable.musculos, 1),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 10.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(50.dp, Alignment.CenterVertically)
    ) {
        routineList.forEachIndexed { index, (exercise, image, _) ->
            // Visibilidad controlada con delay
            var visible = remember { androidx.compose.runtime.mutableStateOf(false) }

            // Lanzar animación en cascada
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(150)
                kotlinx.coroutines.delay(index * 150L) // Delay escalonado
                visible.value = true
            }

            AnimatedVisibility(
                visible = visible.value,
                enter = slideInHorizontally(
                    initialOffsetX = { it } // desde abajo
                ) + fadeIn(animationSpec = tween(1000)),
                exit = slideOutHorizontally(
                    targetOffsetX = { it }
                ) + fadeOut(animationSpec = tween(800))
            ) {
                GetRoutineTypeBox(
                    exercise = exercise,
                    exerciseImage = image,
                    navigationActions = navigationActions,
                    gymViewModel = gymViewModel,
                    authRepository = authRepository
                )
            }
        }
    }
}