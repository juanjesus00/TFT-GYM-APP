package components.box

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tft_gym_app.ui.theme.darkDetailColor
import com.example.tft_gym_app.ui.theme.detailsColor
import components.chart.SmallHistoryChart
import components.langSwitcher.getStringByName
import components.newbox.ViewModelBox
import firebase.auth.AuthRepository
import model.Widget


@Composable
fun Box(authRepository: AuthRepository = viewModel(), widget: Widget){
    val gradient = Brush.linearGradient(
        colors = listOf(darkDetailColor, darkDetailColor),
        start = Offset(20f, 10f),
        end = Offset(20f, 50f)
    )

    var history by remember { mutableStateOf(emptyList<model.Registro>()) }
    if(widget.type == "chart"){
        LaunchedEffect(Unit) {
            authRepository.getHistoryUser( exercise = widget.exercise, onResult = { value ->
                value?.let{ history = it}
            })
        }
    }

    Column (
        modifier = Modifier
            .size(width = 350.dp, height = 250.dp)
            .background(
                brush = gradient,
                shape = RoundedCornerShape(20.dp),
                alpha = 0.5f
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(text = widget.exercise, modifier = Modifier.fillMaxWidth().padding(start = 20.dp, top = 5.dp))
        if (widget.type == "chart") SmallHistoryChart(history = history, exerciseName = widget.exercise)
    }

}