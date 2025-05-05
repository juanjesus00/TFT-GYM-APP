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

@Composable
fun HamburgerMenu(
    navigationActions: NavigationActions,
    navController: NavController,
    isMenuVisible: Boolean,
    onDismiss: () -> Unit
){
    val languageMenuExpanded = remember { mutableStateOf(false) }
    Box (modifier = Modifier
        .offset(x = 10.dp, y = 90.dp)

    ){
        DropdownMenu (
            expanded = isMenuVisible,
            onDismissRequest = onDismiss,
            modifier = Modifier
                .size(width = 150.dp, height = 250.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF161818), shape = RoundedCornerShape(20.dp))
                .border(width = 1.dp, color = Color(0xFFD78323), shape = RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp)
        ){
            GetItem("my_data", 0, navigationActions, LocalContext, onClick = {})
            GetItem("favoriote_videos", 0, navigationActions, LocalContext, onClick = {})
            GetItem("routines", 0, navigationActions, LocalContext, onClick = {})
            GetItem("record", 0, navigationActions, LocalContext, onClick = {})
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