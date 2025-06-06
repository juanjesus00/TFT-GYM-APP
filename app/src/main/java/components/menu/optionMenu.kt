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
import components.buttons.GetOptionButton
import components.langSwitcher.getStringByName
import firebase.auth.AuthRepository
import routes.NavigationActions
import viewModel.auth.AuthViewModel

@Composable
fun GetOptionMenu(
    isMenuVisible: Boolean,
    onDismiss: () -> Unit,
    onClickAction1: () -> Unit,
    actionText1: String,
    onClickAction2: () -> Unit,
    actionText2: String
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
                getStringByName(LocalContext.current, actionText1)?.let{
                    GetOptionButton(
                        text = it,
                        onClick = {
                            onClickAction1.invoke()
                        }
                    )
                }
                getStringByName(LocalContext.current, actionText2)?.let{
                    GetOptionButton(
                        text = it,
                        onClick = {
                            onClickAction2.invoke()
                        }
                    )
                }

            }
        }
    }
}
