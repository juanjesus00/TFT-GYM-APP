package components.menu

import android.content.Context
import android.widget.PopupMenu
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.WhitePoint
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.navigation.NavController
import components.langSwitcher.getStringByName
import routes.NavigationActions

@Composable
fun getMenuVideo(
    navigationActions: NavigationActions,
    navController: NavController,
    isMenuVisible: Boolean,
    onDismiss: () -> Unit
){
    if(isMenuVisible){
        Dialog(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier
                    .size(width = 300.dp, height = 200.dp)
                    .background(color = Color(0xFF161818), shape = RoundedCornerShape(20.dp)),
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GetMenuVideoButton(onClick = {navigationActions.navigateToVideoUploader()}, text = "analyze_new_video")
                GetMenuVideoButton(onClick = {}, text = "new_routine")
            }
        }
    }
}
@Composable
fun GetMenuVideoButton(onClick: (() -> Unit)? = null, text: String){
    Button(
        modifier = Modifier
            .width(200.dp),
        onClick = {
            onClick?.invoke()
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2A2C38),
            contentColor = Color(0xFFD78323)
        )
    ) {
        getStringByName(LocalContext.current, text)?.let {
            Text(text = it)
        }
    }
}