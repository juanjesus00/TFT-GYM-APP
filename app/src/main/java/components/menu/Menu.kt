package components.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import components.langSwitcher.getStringByName
import routes.NavigationActions
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import uiPrincipal.LanguageManager
import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.ui.draw.shadow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import viewModel.auth.AuthViewModel

@Composable
fun HamburgerMenu(
    navigationActions: NavigationActions,
    navController: NavController,
    isMenuVisible: Boolean,
    onDismiss: () -> Unit,
    viewModel: AuthViewModel = viewModel()
){
    val currentUser = Firebase.auth.currentUser
    val languageMenuExpanded = remember { mutableStateOf(false) }
    Box (modifier = Modifier
        .offset(x = 10.dp, y = 90.dp)

    ){
        DropdownMenu (
            expanded = isMenuVisible,
            onDismissRequest = onDismiss,
            modifier = Modifier
                .size(width = 150.dp, height = if(currentUser != null) 320.dp else 160.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF161818), shape = RoundedCornerShape(20.dp))
                .border(width = 1.dp, color = Color(0xFFD78323), shape = RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp)
        ){
            if( currentUser != null){
                GetItem("log_out", 0, navigationActions, LocalContext, onClick = {viewModel.logOut(onSuccess = {navigationActions.navigateToHome()})})
                GetItem("my_data", 0, navigationActions, LocalContext, onClick = {navigationActions.navigateToUserProfile()})
                GetItem("favoriote_videos", 0, navigationActions, LocalContext, onClick = {})
                GetItem("routines", 0, navigationActions, LocalContext, onClick = {})
                GetItem("record", 0, navigationActions, LocalContext, onClick = {})
            }else{
                GetItem("login_button", 0, navigationActions, LocalContext, onClick = {navigationActions.navigateToLogin()})
                GetItem("register_button", 0, navigationActions, LocalContext, onClick = {navigationActions.navigateToRegister()})
            }
            GetItem("language", 0, navigationActions, LocalContext, onClick = {
                if (LanguageManager.languageCode != "en"){
                    LanguageManager.languageCode = "en"
                }else{
                    LanguageManager.languageCode = ""
                }}
            )
        }
    }
}

@Composable
fun GetItem(
    text: String,
    icon: Int,
    navigationActions: NavigationActions,
    context: ProvidableCompositionLocal<Context>,
    onClick: (() -> Unit)? = null
){

    DropdownMenuItem(

        onClick = {
            onClick?.invoke()
        },

        text = {

            getStringByName(context.current, text)?.let {
                Text(text = it, color = Color(0xFFD78323))
            }

        }
    )
}