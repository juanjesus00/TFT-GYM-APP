package components.newbox

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale.Companion.Crop
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tft_gym_app.R
import com.google.firebase.auth.FirebaseAuth
import components.menu.CascadingPopup
import kotlinx.coroutines.launch

@Composable
fun GetBox(viewModel: ViewModelBox = viewModel()) {
    var isVisible by remember { mutableStateOf(false) }
    var anchorBounds by remember { mutableStateOf<Rect?>(null) }
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(LocalDensity.current) {
        configuration.screenWidthDp.dp.toPx()
    }
    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    Box(
        modifier = Modifier
    ) {

        Column (

            modifier = Modifier
                .scale(scale.value)
                .width(100.dp)
                .height(100.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(Color(0xFF161818))
                .onGloballyPositioned { layoutCoordinates ->
                    val bounds = layoutCoordinates.boundsInWindow()
                    anchorBounds = Rect(bounds.topLeft, bounds.size)
                }
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
                        isVisible = !isVisible
                    }

                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center

        ){
            Image(
                modifier = Modifier.size(40.dp),
                painter = painterResource(id = R.drawable.add),
                contentDescription = "more Boxes",
                contentScale = Crop
            )

        }
        CascadingPopup(isVisible = isVisible, anchorBounds = anchorBounds, screenWidthPx = screenWidthPx, onDismiss = {isVisible = false}, listIcons = listOf(R.drawable.chart, R.drawable.schedule, R.drawable.video, R.drawable.pr))
    }

}