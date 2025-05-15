package pages

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tft_gym_app.R
import components.buttons.GetDefaultButton
import components.inputs.GetInputLogin
import components.langSwitcher.getStringByName
import routes.NavigationActions

@Composable
fun GetVideoPage(
    scrollState: ScrollState,
    navigationActions: NavigationActions,
    navController: NavController
) {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(text = "Sube un video")
        Box (
            modifier = Modifier
                .size(width = 350.dp, height = 250.dp)
                .background(color = Color(0x1A000000), shape = RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ){
            Icon(
                painter = painterResource(id = R.drawable.video),
                contentDescription = "video",
                modifier = Modifier.background(color = Color(0x40000000))
            )
        }

        getStringByName(LocalContext.current, "upload_video")?.let{
            GetDefaultButton(text = it, onClick = {})
        }

        repeat(2){
            GetInputLogin(
                text = "test",
                onValueChange = { },
                label = "test label",
                placeholder = "texto de prueba"
            )
        }
    }
}