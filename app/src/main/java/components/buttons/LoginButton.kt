package components.buttons

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import components.langSwitcher.getStringByName

@Composable
fun GetLoginButton(email: String, password: String, onLogin: () -> Unit){
    Button(
        modifier = Modifier,
        onClick = { onLogin.invoke()}
    ) {
        getStringByName(LocalContext.current, "login_button")?.let {
            Text( text = it )
        }
    }
}