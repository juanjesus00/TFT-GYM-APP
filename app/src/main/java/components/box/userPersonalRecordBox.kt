package components.box

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GetUserPersonalRecordBox(){
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFFD78323), Color(0xFF27DD03)),
        start = Offset(25f, 25f),
        end = Offset(100f, 100f)
    )
    /*val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFFD78323), Color(0xFF27DD03)),
        start = Offset(25f, 25f),
        end = Offset(100f, 100f)
    )
    Box (
        modifier = Modifier
            .size(100.dp)
            .background(color = Color(0xFF161818), shape = CircleShape),
        contentAlignment = Alignment.Center,
    ){
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(brush = gradient, fontSize = 15.sp, fontWeight = FontWeight.Bold)) {
                    append("sentadilla")
                }
            }
        )

    }*/
    var expanded by remember { mutableStateOf(false) }

    val width by animateDpAsState(
        targetValue = if (expanded) 350.dp else 100.dp,
        animationSpec = tween(durationMillis = 500),
        label = "Width Animation"
    )

    Box(
        modifier = Modifier
            .width(width)
            .height(100.dp)
            .clip(RoundedCornerShape(50)) // Círculo cerrado + extremos redondeados al expandirse
            .background(if (expanded) Color(0xFFD78323) else Color(0xFF161818))
            .clickable { expanded = !expanded },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            // Círculo fijo
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF161818)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(brush = gradient, fontSize = 15.sp, fontWeight = FontWeight.Bold)) {
                            append("sentadilla")
                        }
                    }
                )
            }

            // Contenido extra, visible solo si está expandido
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(animationSpec = tween(300)) + expandHorizontally(),
                exit = fadeOut(animationSpec = tween(300)) + shrinkHorizontally()
            ) {
                Row(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InfoColumn(title = "Record", value = "140 kg")
                    InfoColumn(title = "Nivel", value = "Élite")
                    InfoColumn(title = "Rareza", value = "0.5%")
                }
            }
        }
    }
}

@Composable
fun InfoColumn(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(text = value, color = Color.White, fontSize = 12.sp)
    }
}