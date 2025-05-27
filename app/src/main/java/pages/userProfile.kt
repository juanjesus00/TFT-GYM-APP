package pages

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import components.userProfileComponents.GetDeleteUserUi
import components.userProfileComponents.GetProfileImage
import components.userProfileComponents.GetUserPersonalRecords
import components.userProfileComponents.GetUserStats
import routes.NavigationActions

@Composable
fun GetUserProfile(
    scrollState: ScrollState,
    navigationActions: NavigationActions,
    navController: NavController
) {
    val user = Firebase.auth.currentUser

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFF2A2C38))
            .verticalScroll(state = scrollState),
        verticalArrangement = Arrangement.spacedBy(30.dp)
    ){
        GetProfileImage(selectImageUri = null, onEditClick = {navigationActions.navigateToEditUserInfo()}, imageSize = 150, editIconSize = 48, startPadding = 30, topPadding = 100)
        GetUserStats()
        GetUserPersonalRecords()
        GetDeleteUserUi(navigationActions, navController)

    }

}