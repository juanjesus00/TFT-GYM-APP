@file:Suppress("INFERRED_TYPE_VARIABLE_INTO_EMPTY_INTERSECTION_WARNING")

package pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import components.inputs.GetInputLogin
import routes.NavigationActions
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import components.buttons.GetGoogleButton
import components.buttons.GetLoginButton
import components.langSwitcher.getStringByName
import components.registerSuggest.GetRegisterSuggest
import components.separator.GetLoginSeparator
import viewModel.auth.AuthViewModel

@Composable
fun GetLoginScreen(navigationActions: NavigationActions, navController: NavHostController, viewModel: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2A2C38))
            .padding(top = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        getStringByName(context, "login_welcome_text")?.let {
            Text(text = it , color = Color(0xFFD78323))
        }

        GetInputLogin(
            text = email,
            onValueChange = { email = it },
            label = "test label",
            placeholder = "texto de prueba"
        )
        GetInputLogin(
            text = password,
            onValueChange = { password = it },
            label = "test label",
            placeholder = "texto de prueba"
        )

        GetLoginButton(
            email = email,
            password = password,
            onLogin = { viewModel.login(email = email, password = password, context = context, navigationActions = navigationActions, onSuccess = {navigationActions.navigateToHome()}, onErrorAction = {/*TODO*/}) }
        )
        if (Firebase.auth.currentUser != null){
            Text(text = "tenemos login = ${Firebase.auth.currentUser!!.email ?:"Sin correo"}")
        }
        GetLoginSeparator()

        GetGoogleButton()

        GetRegisterSuggest()
    }
}
