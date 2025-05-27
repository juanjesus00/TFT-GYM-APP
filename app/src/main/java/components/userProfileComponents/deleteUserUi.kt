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
import androidx.navigation.NavController
import components.buttons.GetDefaultButton
import components.langSwitcher.getStringByName
import components.menu.GetOptionMenu
import routes.NavigationActions

@Composable
fun GetDeleteUserUi(navigationActions: NavigationActions, navController: NavController) {
    var isVisible by remember { mutableStateOf(false) }
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
            navigationActions = navigationActions,
            navController = navController
        )
    }
}