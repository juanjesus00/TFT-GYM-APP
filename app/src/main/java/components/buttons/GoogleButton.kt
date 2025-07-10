package components.buttons

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.tft_gym_app.R

@Composable
fun GetGoogleButton(){
    IconButton(
        modifier = Modifier
            .size(50.dp),
        onClick = {}
    ) {
        Icon(
            painter = painterResource(id = R.drawable.googleicon),
            contentDescription = "add",
            tint = Color.Unspecified,
            modifier = Modifier.fillMaxSize()
        )
    }
}
