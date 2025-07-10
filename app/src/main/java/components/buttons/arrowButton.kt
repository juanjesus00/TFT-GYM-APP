package components.buttons

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import com.example.tft_gym_app.R

@Composable
fun GetArrowButton(onchange: () -> Unit, isExpanded: Boolean) {
    val rotationAngle by animateFloatAsState(if (isExpanded) 90f else 0f)

    IconButton(
        onClick = {
            onchange()
        }
    ) {
        Icon(
            painter = painterResource(id = R.drawable.arrowdropdown),
            contentDescription = "dropdown arrow",
            modifier = Modifier.rotate(rotationAngle)
        )
    }
}