package components.menu

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import components.buttons.GetOptionButton
import components.langSwitcher.getStringByName
import components.newbox.ViewModelBox
import firebase.auth.AuthRepository

@Composable
fun GetOptionBoxMenu(
    iconIndex: Int,
    isMenuVisible: Boolean,
    onDismiss: () -> Unit,
    authRepository: AuthRepository = viewModel(),
    viewModelBox: ViewModelBox = viewModel()
){
    val exerciseList = listOf("bench_press", "dead_lift", "squad")
    if(isMenuVisible){
        Dialog(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier
                    .size(width = 300.dp, height = 400.dp)
                    .background(color = Color(0xFF161818), shape = RoundedCornerShape(20.dp)),
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when(iconIndex){
                    0 -> {
                        Text(text = "Elige un ejercicio")
                        exerciseList.forEach { exercise ->
                            getStringByName(LocalContext.current, exercise)?.let{
                                GetOptionButton(
                                    text = it,
                                    onClick = {
                                        authRepository.addNewWidget(widgetType = "chart", exercise = it, onSuccess = {Log.d("loginbackend", "Creado ${it}")}, viewModelBox)
                                        onDismiss.invoke()
                                    }
                                )
                            }
                        }

                    }
                    1 -> {
                        Text(text = "Elige un ejercicio")
                        exerciseList.forEach { exercise ->
                            getStringByName(LocalContext.current, exercise)?.let{
                                GetOptionButton(
                                    text = it,
                                    onClick = {
                                        authRepository.addNewWidget(widgetType = "calendario", exercise = it, onSuccess = {Log.d("loginbackend", "Creado ${it}")}, viewModelBox)
                                        onDismiss.invoke()
                                    }
                                )
                            }
                        }
                    }
                    2 -> {
                        Text(text = "Elige un ejercicio")
                        exerciseList.forEach { exercise ->
                            getStringByName(LocalContext.current, exercise)?.let{
                                GetOptionButton(
                                    text = it,
                                    onClick = {
                                        authRepository.addNewWidget(widgetType = "video", exercise = it, onSuccess = {Log.d("loginbackend", "Creado ${it}")}, viewModelBox)
                                        onDismiss.invoke()
                                    }
                                )
                            }
                        }
                    }
                    3 -> {
                        Text(text = "Elige un ejercicio")
                        exerciseList.forEach { exercise ->
                            getStringByName(LocalContext.current, exercise)?.let{
                                GetOptionButton(
                                    text = it,
                                    onClick = {
                                        authRepository.addNewWidget(widgetType = "pr", exercise = it, onSuccess = {Log.d("loginbackend", "Creado ${it}")}, viewModelBox)
                                        onDismiss.invoke()
                                    }
                                )
                            }
                        }
                    }
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
