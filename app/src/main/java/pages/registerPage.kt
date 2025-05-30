package pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import components.buttons.GetGoogleButton
import components.buttons.GetLoginButton
import components.checkBoxs.GetRegisterCheckBox
import components.inputs.GetInputLogin
import components.inputs.PasswordInputField
import components.langSwitcher.getStringByName
import components.registerSuggest.GetRegisterSuggest
import components.separator.GetLoginSeparator
import routes.NavigationActions
import viewModel.api.GymViewModel
import viewModel.auth.AuthViewModel

@Composable
fun GetRegisterScreen(navigationActions: NavigationActions, navController: NavHostController, gymViewModel: GymViewModel,viewModel: AuthViewModel = viewModel()){
    var nickName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var isChecked by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2A2C38))
            .padding(top = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        getStringByName(context, "register_welcome_text")?.let {
            Text(text = it , color = Color(0xFFD78323))
        }
        getStringByName(context, "user_name")?.let{label ->
            GetInputLogin(
                text = nickName,
                onValueChange = { nickName = it },
                label = label,
                placeholder = label
            )
        }
        getStringByName(context, "email")?.let{label ->
            GetInputLogin(
                text = email,
                onValueChange = { email = it },
                label = label,
                placeholder = label
            )
        }
        getStringByName(context, "password")?.let{label ->
            PasswordInputField(
                password = password,
                text = label,
                onPasswordChange = { password = it },
            )
        }
        getStringByName(context, "repeat_password")?.let{label ->
            PasswordInputField(
                password = repeatPassword,
                text = label,
                onPasswordChange = { repeatPassword = it },
            )
        }

        GetRegisterCheckBox(
            isChecked = isChecked,
            onValueChange = { isChecked = it }
        )
        GetLoginButton(
            buttonText = "register_button",
            onLogin = { viewModel.register(email = email, password = password, repeatPassword = repeatPassword, name = nickName, context = context, onSuccess = {navigationActions.navigateToUserData()}, navigationActions = navigationActions) }
        )

        GetLoginSeparator()

        GetGoogleButton()

        GetRegisterSuggest(
            questionText = "login_suggest",
            linkText = "login_link",
            onLink = {navigationActions.navigateToLogin()}
        )
    }
}