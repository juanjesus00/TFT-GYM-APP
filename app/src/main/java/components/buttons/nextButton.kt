package components.buttons

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tft_gym_app.ui.theme.detailsColor
import components.langSwitcher.getStringByName

@Composable
fun GetNextButton(buttonText: String, enable: Boolean, onNextButton: () -> Unit){
    Button(
        modifier = Modifier.width(280.dp).height(50.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = detailsColor,
            contentColor = Color.White
        ),
        enabled = enable,
        onClick = { onNextButton.invoke()}
    ) {
        getStringByName(LocalContext.current, buttonText)?.let {
            Text( text = it )
        }
    }
}