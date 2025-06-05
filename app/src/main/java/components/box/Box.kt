package components.box

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tft_gym_app.ui.theme.darkDetailColor
import components.newbox.ViewModelBox
import model.Widget


@Composable
fun Box(viewModel: ViewModelBox = viewModel(), widget: Widget){
    val gradient = Brush.linearGradient(
        colors = listOf(darkDetailColor, darkDetailColor),
        start = Offset(0f, 0f),
        end = Offset(100f, 100f)
    )
    Column (
        modifier = Modifier
            .size(width = 350.dp, height = 210.dp)
            .background(
                brush = gradient,
                shape = RoundedCornerShape(20.dp),
                alpha = 0.2f
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(text = "${widget.type} de ${widget.exercise}")
    }

}