package components.buttons

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
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
fun GetOptionButton(text: String, onClick:() -> Unit){
    Button(
        onClick = {
            onClick.invoke()
        },
        modifier = Modifier.width(280.dp).height(50.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = detailsColor,
            contentColor = Color.White
        )
    ) {
        Text(text = text)
    }
}