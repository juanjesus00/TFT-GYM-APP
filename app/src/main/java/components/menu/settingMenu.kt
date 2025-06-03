package components.menu

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import firebase.auth.AuthRepository
import model.Registro
import routes.NavigationActions
import viewModel.api.GymViewModel

@Composable
fun GetSettingMenu(
    navigationActions: NavigationActions,
    navController: NavController,
    index: Int,
    history: List<Registro>,
    exercise: String?,
    authRepository: AuthRepository = viewModel(),
    gymViewModel: GymViewModel = viewModel()
) {

    var isVisible by remember { mutableStateOf(false) }
    var editableMenu by remember { mutableStateOf(false) }
    var anchorBounds by remember { mutableStateOf<Rect?>(null) }
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(LocalDensity.current) {
        configuration.screenWidthDp.dp.toPx()
    }
    val rm = history[index].rm
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
        navController = navController,
        navigationActions = navigationActions,
        onClickAction1 = {
            isVisible = false
            editableMenu = true
                         },
        actionText1 = "edit",
        onClickAction2 = {
            isVisible = false
            val newHistory = history.toMutableList()
            newHistory.removeAt(index)
            getStringByName(context, exercise?:"")?.let{
                authRepository.updateHistoryUser(history = newHistory, rm = rm, exercise = it, onSuccess = {navigationActions.navigateToHistory()})
            }
                         },
        actionText2 = "delete")

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


}