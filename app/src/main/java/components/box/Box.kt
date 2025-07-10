package components.box

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tft_gym_app.ui.theme.darkDetailColor
import components.chart.SmallHistoryChart
import components.menu.GetOptionMenu
import components.newbox.ViewModelBox
import components.personalRecord.GetPersonalRecordWidget
import firebase.auth.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import model.Widget


@Composable
fun Box(authRepository: AuthRepository = viewModel(), widget: Widget, viewModel: ViewModelBox = viewModel()){
    val gradient = Brush.linearGradient(
        colors = listOf(darkDetailColor, darkDetailColor),
        start = Offset(20f, 10f),
        end = Offset(20f, 50f)
    )
    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    var isMenuVisible by remember {mutableStateOf(false)}
    val haptic = LocalHapticFeedback.current
    var history by remember { mutableStateOf(emptyList<model.Registro>()) }
    if(widget.type == "chart"){
        LaunchedEffect(Unit) {
            authRepository.getHistoryUser( exercise = widget.exercise, onResult = { value ->
                value?.let{ history = it}
            })
        }
    }

    var listExercise by remember { mutableStateOf(emptyMap<String, Float>()) }
    var listLevelRare by remember { mutableStateOf(emptyMap<String, Map<String, Any>>()) }
    if(widget.type == "pr"){
        LaunchedEffect(Unit) {
            authRepository.getRMUser(onResult = { value ->
                value?.let{ listExercise = it}
            })
            Log.d("Personal Records", "$listExercise")

            authRepository.getLevelRare { value ->
                value?.let{
                    listLevelRare = it
                }
            }
        }
    }


    Column (
        modifier = Modifier
            .graphicsLayer(
                scaleX = scale.value,
                scaleY = scale.value
            )
            .size(width = 350.dp, height = 250.dp)
            .background(
                brush = gradient,
                shape = RoundedCornerShape(20.dp),
                alpha = 0.5f
            )
            .pointerInput(Unit) {
                while (true) {
                    awaitPointerEventScope {
                        val down = awaitFirstDown()

                        // Empezar animación de hundimiento
                        coroutineScope.launch {
                            scale.animateTo(0.90f, tween(150))
                        }

                        var triggered = false
                        val job = coroutineScope.launch {
                            delay(1000L) // Esperar 1 segundo
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isMenuVisible = true
                            triggered = true
                        }

                        val up = waitForUpOrCancellation()
                        job.cancel() // Cancelar si se soltó antes

                        coroutineScope.launch {
                            scale.animateTo(1f, tween(150)) // Siempre volver a tamaño original
                        }

                        if (!triggered) {
                            isMenuVisible = false // Asegura que no se muestra si no se mantuvo 1s
                        }
                    }
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ){

        when(widget.type){
            "chart" -> {
                Text(text = widget.exercise, modifier = Modifier.fillMaxWidth().padding(start = 20.dp, top = 5.dp))
                SmallHistoryChart(history = history, exerciseName = widget.exercise)
            }
            "pr" -> {
                GetPersonalRecordWidget(widget = widget, listExercise = listExercise, listLevelRare = listLevelRare)
            }
        }

    }

    GetOptionMenu(
        isMenuVisible = isMenuVisible,
        onDismiss = { isMenuVisible = false },
        onClickAction1 = {
            authRepository.deleteWidgetFromFirebase(widgetId = widget.id)
            viewModel.removeWidget(widgetId = widget.id)
            isMenuVisible = false
        },
        actionText1 = "delete",
        onClickAction2 = { isMenuVisible = false },
        actionText2 = "cancel"
    ) {}
}