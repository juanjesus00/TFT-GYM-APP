package components.userProfileComponents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import components.buttons.GetDefaultButton
import components.langSwitcher.getStringByName
import components.menu.GetOptionMenu
import firebase.auth.AuthRepository
import routes.NavigationActions

@Composable
fun GetDeleteUserUi(navigationActions: NavigationActions, navController: NavController, viewModelRepository: AuthRepository = viewModel()) {
    var isVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Box (
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ){
        getStringByName(LocalContext.current, "delete_user")?.let{
            GetDefaultButton(text = it, onClick = {isVisible = !isVisible}, enabled = true)
        }
        GetOptionMenu(
            isMenuVisible = isVisible,
            onDismiss = { isVisible = false},
            onClickAction1 = {viewModelRepository.deleteUser(context = context, onSuccess = {navigationActions.navigateToHome()})},
            actionText1 = "accept",
            onClickAction2 = {isVisible = false},
            actionText2 = "cancel"
        )
    }
}