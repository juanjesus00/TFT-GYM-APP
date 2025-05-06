package components.menu

import androidx.compose.ui.geometry.Rect
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tft_gym_app.R
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale.Companion.Crop
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.delay
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import components.newbox.ViewModelBox

@Composable
fun CascadingPopup(
    isVisible: Boolean,
    anchorBounds: Rect?,
    screenWidthPx: Float,
    onDismiss: () -> Unit,
    viewModel: ViewModelBox = viewModel()
) {
    if (!isVisible || anchorBounds == null) return

    val density = LocalDensity.current
    val popupWidthDp = 100.dp
    val popupWidthPx = with(density) { popupWidthDp.toPx() }

    val iconHeightDp = 60.dp
    val popupTotalHeightDp = iconHeightDp * 3 + 24.dp // 3 íconos + padding
    val popupTotalHeightPx = with(density) { popupTotalHeightDp.toPx() }

    val spaceToRight = screenWidthPx - (anchorBounds.right + popupWidthPx)
    val spaceToLeft = screenWidthPx - (anchorBounds.left - popupWidthPx)

    val offsetXPx = when {
        spaceToRight > 0 -> anchorBounds.left + 120.0// suficiente espacio a la derecha
        spaceToLeft > 0 -> anchorBounds.left - popupWidthPx // si no, a la izquierda
        else -> anchorBounds.left // fallback centrado encima
    }

    // Centrado vertical del popup respecto al componente ancla
    val offsetYPx = anchorBounds.top + (anchorBounds.height / 2) - (popupTotalHeightPx / 2)

    Popup(
        offset = IntOffset(offsetXPx.toInt(), offsetYPx.toInt()),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true, clippingEnabled = false)
    ) {
        // CONTENIDO DEL POPUP
        Column(
            modifier = Modifier
                .width(popupWidthDp)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val icons = listOf(R.drawable.chart, R.drawable.schedule, R.drawable.video, R.drawable.pr)
            icons.forEachIndexed { index, iconRes ->
                AnimatedIconWithCascade(iconRes, index, viewModel)
            }
        }
    }
}



@Composable
fun AnimatedIconWithCascade(iconRes: Int, index: Int, viewModel: ViewModelBox) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * 300L) // Espera que el anterior termine
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
    )

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Box(
        modifier = Modifier
            .size(70.dp)
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
            }
            .clip(CircleShape)
            .background(Color(0xFF161818))
            .clickable {
                viewModel.addBox()

                       },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(33.dp),
            contentScale = Crop
        )
    }
}