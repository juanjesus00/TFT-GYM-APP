package components.box

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tft_gym_app.ui.theme.darkDetailColor
import components.langSwitcher.getStringByName
import firebase.auth.AuthRepository
import kotlinx.coroutines.launch
import routes.NavigationActions
import viewModel.api.GymViewModel

@Composable
fun GetRoutineTypeBox(
    routineType: String,
    exerciseImage: Int,
    navigationActions: NavigationActions,
    gymViewModel: GymViewModel,
    authRepository: AuthRepository
){
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFFD78323), Color(0xFF27DD03)),
        start = Offset(25f, 25f),
        end = Offset(100f, 100f)
    )

    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }


    Row (
        modifier = Modifier
            .graphicsLayer(
                scaleX = scale.value,
                scaleY = scale.value
            )
            .width(300.dp)
            .height(140.dp)
            .background(color = darkDetailColor, shape = RoundedCornerShape(20.dp))
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
                    navigationActions.navigateToRoutinePage()
                    gymViewModel.actualizarRoutineType(routineType)
                }

            },
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Box(
            modifier = Modifier
                .padding(start=20.dp)
                .size(80.dp)
                .background(color = Color(0xFFD9D9D9), shape = RoundedCornerShape(40.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier
                    .size(50.dp),
                painter = painterResource(exerciseImage),
                contentDescription = "exerciseImage",
                contentScale = ContentScale.Crop

            )
        }

        getStringByName(LocalContext.current, routineType)?.let{
            Text(
                buildAnnotatedString {
                    withStyle(style = SpanStyle(brush = gradient, fontSize = 20.sp, fontWeight = FontWeight.Bold)) {
                        append(it)
                    }
                })
        }
    }
}