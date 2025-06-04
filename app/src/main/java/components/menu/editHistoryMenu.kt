package components.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import components.buttons.GetOptionButton
import components.inputs.GetInputLogin
import components.langSwitcher.getStringByName
import firebase.auth.AuthRepository
import model.Registro
import routes.NavigationActions
import viewModel.api.GymViewModel

@Composable
fun GetEditHistoryMenu(
    navigationActions: NavigationActions,
    navController: NavController,
    isMenuVisible: Boolean,
    onDismiss: () -> Unit,
    onAccept: String,
    gymViewModel: GymViewModel = viewModel(),
    authRepository: AuthRepository = viewModel(),
    index: Int,
    history: List<Registro>,
    exercise: String?
){
    var date by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var repetitions by remember { mutableStateOf("") }
    var rm by remember { mutableStateOf("") }
    val context = LocalContext.current
    if(isMenuVisible){
        Dialog(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier
                    .size(width = 350.dp, height = 550.dp)
                    .background(color = Color(0xFF161818), shape = RoundedCornerShape(20.dp)),
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                getStringByName(context, exercise.toString())?.let{
                    Text( text = it)
                }

                getStringByName(LocalContext.current, "date")?.let{ label ->
                    GetInputLogin(
                        text = date,
                        onValueChange = { date = it },
                        label = label,
                        placeholder = "YYYY-MM-DD"
                    )
                }

                getStringByName(LocalContext.current, "weight")?.let{ label ->
                    GetInputLogin(
                        text = weight,
                        onValueChange = { weight = it },
                        label = label,
                        placeholder = label
                    )
                }

                getStringByName(LocalContext.current, "repetitions")?.let{ label ->
                    GetInputLogin(
                        text = repetitions,
                        onValueChange = { repetitions = it },
                        label = label,
                        placeholder = label
                    )
                }

                getStringByName(LocalContext.current, "rm")?.let{ label ->
                    GetInputLogin(
                        text = rm,
                        onValueChange = { rm = it },
                        label = label,
                        placeholder = label
                    )
                }

                getStringByName(LocalContext.current, "accept")?.let{
                    GetOptionButton(
                        text = it,
                        onClick = {
                            getStringByName(context, exercise?:"")?.let {
                                when(onAccept){
                                    "add" -> {
                                        val newHistory = history.toMutableList()
                                        newHistory.add(model.Registro(fecha = date, peso = weight.toFloat(), repeticiones = repetitions.toInt(), rm = rm.toFloat()))
                                        authRepository.updateHistoryUser(history = newHistory, rm = rm.toFloat(), exercise = it, onSuccess = {navigationActions.navigateToHistory()})
                                    }//authRepository.editUserFromVideo(exercise = it, date = date, weight = weight.toFloat(), repetitions = repetitions.toInt(), rm = rm.toFloat(), onSuccess = {navigationActions.navigateToHistory()})
                                    "edit" -> {
                                        history.forEachIndexed { i, item ->
                                            if (i == index) {
                                                item.fecha = date
                                                item.peso = weight.toFloat()
                                                item.repeticiones = repetitions.toInt()
                                                item.rm = rm.toFloat()
                                            }
                                        }
                                        authRepository.updateHistoryUser(history = history, rm = rm.toFloat(), exercise = it, onSuccess = {navigationActions.navigateToHistory()})
                                    }

                                }
                            }

                        }
                    )
                }

                getStringByName(LocalContext.current, "cancel")?.let{
                    GetOptionButton(
                        text = it,
                        onClick = {
                            onDismiss.invoke()
                        }
                    )
                }


            }
        }
    }
}