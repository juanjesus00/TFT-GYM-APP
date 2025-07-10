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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale.Companion.Crop
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.delay
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import components.newbox.ViewModelBox

@Composable
fun CascadingPopup(
    isVisible: Boolean,
    anchorBounds: Rect?,
    screenWidthPx: Float,
    onDismiss: () -> Unit,
    listIcons: List<Int>,
    onIconClick: (Int) -> Unit
) {
    if (!isVisible || anchorBounds == null) return

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val iconHeightDp = 60.dp
    val popupTotalHeightDp = iconHeightDp * 3  // 3 íconos + padding
    val popupTotalHeightPx = with(density) { popupTotalHeightDp.toPx() }

    val spaceToRight = screenWidthPx - (anchorBounds.right)
    val spaceToLeft = anchorBounds.left

    val offsetXPx = when {
        spaceToRight > 0 -> anchorBounds.left// suficiente espacio a la derecha
        spaceToLeft > 0 -> (anchorBounds.right).coerceAtLeast(0f) // si no, a la izquierda
        else -> (screenWidthPx) / 2 // fallback centrado encima
    }
    // Centrado vertical del popup respecto al componente ancla
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    val spaceBelow = screenHeightPx - anchorBounds.bottom
    val spaceAbove = anchorBounds.top

    val verticalMarginPxBelow = with(density) { 8.dp.toPx() }
    val verticalMarginPxAbove = with(density) { 150.dp.toPx() }
    val offsetYPx = when {
        spaceBelow > popupTotalHeightPx -> anchorBounds.bottom + verticalMarginPxBelow
        spaceAbove > popupTotalHeightPx -> anchorBounds.top - popupTotalHeightPx - verticalMarginPxAbove
        spaceBelow >= spaceAbove -> anchorBounds.bottom + verticalMarginPxBelow
        else -> anchorBounds.top - popupTotalHeightPx - verticalMarginPxAbove
    }
    class CustomOffsetPositionProvider(
        private val x: Int,
        private val y: Int
    ) : PopupPositionProvider {
        override fun calculatePosition(
            anchorBounds: IntRect,
            windowSize: IntSize,
            layoutDirection: LayoutDirection,
            popupContentSize: IntSize
        ): IntOffset {
            return IntOffset(x, y)
        }
    }

    Popup(
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true, clippingEnabled = true),
        popupPositionProvider = CustomOffsetPositionProvider(offsetXPx.toInt(), offsetYPx.toInt())
    ) {
        // CONTENIDO DEL POPUP
        Column(
            modifier = Modifier
                .width(100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            listIcons.forEachIndexed { index, iconRes ->
                AnimatedIconWithCascade(iconRes, index, onDismiss, onClick = {
                    onIconClick(index)  // importante
                    onDismiss()
                })
            }
        }
    }
}



@Composable
fun AnimatedIconWithCascade(
    iconRes: Int,
    index: Int,
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
    var isMenuVisible by remember { mutableStateOf(false) }
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
                onClick.invoke()
                onDismiss()
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