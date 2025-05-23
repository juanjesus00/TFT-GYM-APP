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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import components.buttons.GetNextButton
import components.inputs.GetInputLogin
import components.langSwitcher.getStringByName
import routes.NavigationActions
import viewModel.auth.AuthViewModel

@Composable
fun GetUserDataScreen(navigationActions: NavigationActions, navController: NavHostController, viewModel: AuthViewModel = viewModel()){
    var gender by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    val context = LocalContext.current
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2A2C38))
            .padding(top = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        getStringByName(context, "register_welcome_text")?.let {
            Text(text = it , color = Color(0xFFD78323))
        }

        GetInputLogin(
            text = gender,
            onValueChange = { gender = it },
            label = "test label",
            placeholder = "texto de prueba"
        )
        GetInputLogin(
            text = birthDate,
            onValueChange = { birthDate = it },
            label = "test label",
            placeholder = "texto de prueba"
        )
        GetInputLogin(
            text = weight,
            onValueChange = { weight = it },
            label = "test label",
            placeholder = "texto de prueba"
        )
        GetInputLogin(
            text = height,
            onValueChange = { height = it },
            label = "test label",
            placeholder = "texto de prueba"
        )
        GetNextButton (
            buttonText = "register_button",
            onNextButton = { viewModel.editUser(gender = gender, birthDate = birthDate, weight = weight.toInt(), height = height.toInt(), onSuccess = {navigationActions.navigateToHome()})}
        )
    }
}