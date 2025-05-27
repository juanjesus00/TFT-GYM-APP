package components

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.tft_gym_app.R
import routes.NavigationActions
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import components.menu.GetMenuVideo
import kotlinx.coroutines.launch


@Composable
fun GetNavigatorBar(navigationActions: NavigationActions, navController: NavController){
    var isVideoPress by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    Box (
        modifier = Modifier.fillMaxWidth()
            .padding(bottom = 30.dp),
        contentAlignment = Alignment.Center
    ){
        Row (
            modifier = Modifier
                .size(width = 350.dp, height = 80.dp)
                .background(Color(0xFF161818), shape = RoundedCornerShape(20.dp)),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {navigationActions.navigateToHome()},
                modifier = Modifier.size(60.dp)
            ) {
                Image(modifier = Modifier.size(50.dp),painter = painterResource(R.drawable.ci_house), contentDescription = "house")
            }

            Box(
                modifier = Modifier
                    .scale(scale.value)
                    .zIndex(1f)
                    .size(80.dp)
                    .offset(y=(-35).dp, x=(-5).dp)
                    .clip(RoundedCornerShape(40.dp))
                    .clickable(
                        indication = null, // sin ripple
                        interactionSource = remember { MutableInteractionSource() }
                    ){
                        coroutineScope.launch {
                            // Animación hacia "hundido"
                            scale.animateTo(
                                targetValue = 0.90f,
                                animationSpec = tween(150)
                            )
                            // Animación de rebote
                            scale.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(150)
                            )
                        }
                        isVideoPress = !isVideoPress
                    },
                contentAlignment = Alignment.Center,
            ){
                Image(modifier = Modifier.size(80.dp), painter = painterResource(R.drawable.video_analisis), contentDescription = "video")
            }
            IconButton(
                onClick = {}
            ) {
                Image(painter = painterResource(R.drawable.user_icon), contentDescription = "user")
            }

        }
    }
    Box (){
        GetMenuVideo(navigationActions, navController, isVideoPress, onDismiss = {isVideoPress = false})
    }

}