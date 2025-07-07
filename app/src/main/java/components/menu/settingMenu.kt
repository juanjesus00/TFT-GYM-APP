package components.menu

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import components.langSwitcher.getStringByName
import components.routineComponents.AddRoutineDialog
import firebase.auth.AuthRepository
import model.Registro
import model.RutinaFirebase
import routes.NavigationActions
import viewModel.api.GymViewModel

@Composable
fun GetSettingMenu(
    navigationActions: NavigationActions,
    navController: NavController,
    index: Int,
    history: List<Registro> = listOf(),
    routine: List<RutinaFirebase> = listOf(),
    exercise: String?,
    authRepository: AuthRepository = viewModel(),
    gymViewModel: GymViewModel = viewModel()
) {

    var isVisible by remember { mutableStateOf(false) }
    var editableMenu by remember { mutableStateOf(false) }
    var editableRoutineMenu by remember { mutableStateOf(false) }
    var anchorBounds by remember { mutableStateOf<Rect?>(null) }
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(LocalDensity.current) {
        configuration.screenWidthDp.dp.toPx()
    }
    val context = LocalContext.current


    IconButton(
        modifier = Modifier
            .onGloballyPositioned { layoutCoordinates ->
                val bounds = layoutCoordinates.boundsInWindow()
                anchorBounds = Rect(bounds.topLeft, bounds.size)
            },
        onClick = {
            isVisible = !isVisible
        }
    ) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "setting menu",
            tint = Color(0xFFD9D9D9),
        )
    }
    GetOptionMenu(isMenuVisible = isVisible,
        onDismiss = {isVisible = false},
        onClickAction1 = {
            isVisible = false

            if(!history.isNullOrEmpty()){
                editableMenu = true
            }else if(!routine.isNullOrEmpty()){
                editableRoutineMenu = true
            }
                         },
        actionText1 = "edit",
        onClickAction2 = {
            isVisible = false
            if(!history.isNullOrEmpty()){
                val newHistory = history.toMutableList()
                newHistory.removeAt(index)
                val rm = authRepository.getNewMaxRmFromHistory(newHistory)
                getStringByName(context, exercise?:"")?.let{
                    authRepository.deleteHistoryRegistro(history = newHistory, rm = rm, exercise = it, onSuccess = {navigationActions.navigateToHistory()}, context)
                }
            }else if(!routine.isNullOrEmpty()){
                val newRoutine = routine.toMutableList()
                newRoutine.removeAt(index)
                Log.d("delete Routine", "$newRoutine")
                authRepository.deleteHypertrophyRoutine(newRoutine, onSuccess = {navigationActions.navigateToRoutineSelector()})
            }

                         },
        actionText2 = "delete",
        exercise = exercise,
        onClickAction3 = {
            var newRoutine = routine
            newRoutine.forEachIndexed { i, rutina ->
                if(i != index){
                    if(rutina.activa){
                        rutina.activa = false
                    }
                }
            }
            newRoutine[index].activa = true
            authRepository.activateHypertrophyRoutine(newRoutine.toMutableList(), onSuccess = {navigationActions.navigateToRoutineSelector()})
        })

    GetEditHistoryMenu(
        navigationActions = navigationActions,
        navController = navController,
        isMenuVisible = editableMenu,
        onDismiss = {editableMenu = false},
        onAccept = "edit",
        index = index,
        history = history,
        exercise = exercise
    )
    AddRoutineDialog(
        isVisible = editableRoutineMenu,
        onDismiss = {editableRoutineMenu = false},
        tipo = exercise?:"",
        ejercicio = "",
        index = index,
        routine = routine,
        navigationActions = navigationActions,
        navController = navController
    )

}