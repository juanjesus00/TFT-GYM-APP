package components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.RadioButtonDefaults.colors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.tft_gym_app.ui.theme.detailsColor
import components.langSwitcher.getStringByName

@Composable
fun GetDefaultIconButton(text: String, onClick:() -> Unit, enabled: Boolean, icon: Int){
    IconButton (
        onClick = {
            onClick.invoke()
        },
        enabled = enabled,
        modifier = Modifier.background(color = detailsColor, shape = RoundedCornerShape(20.dp)).width(280.dp).height(50.dp),
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = Color.White
        ),
    ) {
        Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly){
            Text(text = text)
            Icon(painter = painterResource(icon), contentDescription = "icon button")
        }

    }
}

