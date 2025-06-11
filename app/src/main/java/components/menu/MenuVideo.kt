package components.menu

import android.content.Context
import android.widget.PopupMenu
import android.widget.Toast
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.WhitePoint
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import components.langSwitcher.getStringByName
import firebase.auth.AuthRepository
import routes.NavigationActions
import viewModel.auth.AuthViewModel

@Composable
fun GetMenuVideo(
    navigationActions: NavigationActions,
    navController: NavController,
    isMenuVisible: Boolean,
    onDismiss: () -> Unit
){

    val isCurrentUserLogged = FirebaseAuth.getInstance().currentUser?.let{ true } ?: false
    val context = LocalContext.current
    var isEmailVerified by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        FirebaseAuth.getInstance().currentUser?.reload()?.addOnCompleteListener {
            isEmailVerified = FirebaseAuth.getInstance().currentUser?.isEmailVerified == true
        }
    }

    if(isMenuVisible){
        Dialog(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier
                    .size(width = 300.dp, height = 200.dp)
                    .background(color = Color(0xFF161818), shape = RoundedCornerShape(20.dp)),
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GetMenuVideoButton(
                    onClick = {
                        if(isCurrentUserLogged && isEmailVerified) navigationActions.navigateToVideoUploader() else  Toast.makeText(context, "Tienes que iniciar sesión y verificar la cuenta para usar esta función", Toast.LENGTH_LONG).show()
                              },
                    text = "analyze_new_video",
                    enable = true)
                GetMenuVideoButton(
                    onClick = {
                        if(isCurrentUserLogged && isEmailVerified) navigationActions.navigateToRoutinePage() else  Toast.makeText(context, "Tienes que iniciar sesión y verificar la cuenta para usar esta función", Toast.LENGTH_LONG).show()
                    },
                    text = "new_routine",
                    enable = true)
            }
        }
    }
}
@Composable
fun GetMenuVideoButton(onClick: (() -> Unit)? = null, text: String, enable: Boolean){
    Button(
        modifier = Modifier
            .width(200.dp),
        onClick = {
            onClick?.invoke()
        },
        enabled = enable,
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