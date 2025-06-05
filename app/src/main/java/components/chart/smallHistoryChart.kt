package components.chart

import android.text.Layout
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.*
import com.example.tft_gym_app.ui.theme.darkDetailColor
import com.example.tft_gym_app.ui.theme.detailsColor
import components.inputs.GetInputWithDropdown
import kotlinx.coroutines.launch
import model.Registro
import okhttp3.internal.platform.android.AndroidLogHandler.close
import java.text.SimpleDateFormat
import java.util.*
import com.example.tft_gym_app.R

import kotlin.io.path.moveTo


@Composable
fun SmallHistoryChart(
    history: List<Registro>,
    exerciseName: String
) {
    if (history.isEmpty()) {
        Text("No hay datos disponibles para $exerciseName")
        return
    }

    val formatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val now = remember { Date() }

    var selectedFilter by remember { mutableStateOf(TimeFilter.ALL) }


    // UI: Selectores de filtro y tipo de gráfico

    val filteredHistory = remember(history, selectedFilter) {
        history.mapNotNull {
            val date = try {
                formatter.parse(it.fecha)
            } catch (e: Exception) {
                null
            }
            if (date != null) Pair(it, date) else null
        }.filter { (_, date) ->
            val diff = now.time - date.time
            when (selectedFilter) {
                TimeFilter.LAST_7_DAYS -> diff <= 7 * 24 * 60 * 60 * 1000
                TimeFilter.LAST_MONTH -> diff <= 30L * 24 * 60 * 60 * 1000
                TimeFilter.LAST_YEAR -> diff <= 365L * 24 * 60 * 60 * 1000
                TimeFilter.ALL -> true
            }
        }.map { it.first } // Te quedas solo con el Registro
            .sortedBy { formatter.parse(it.fecha) }
    }

    val pesos = filteredHistory.map { it.rm.toFloat() }

    val maxPeso = (pesos.maxOrNull() ?: 1f) + 5
    val minPeso = pesos.minOrNull() ?: 0f

    var touchedIndex by remember { mutableIntStateOf(-1) }

    val rendimiento = remember(pesos) {
        if (pesos.size >= 2) {
            val diff = pesos.last() - pesos.first()
            val percentage = (diff / pesos.first()) * 100
            percentage
        } else null
    }

    val animatedYs = remember(pesos) {
        pesos.map { Animatable(0f) }
    }

    LaunchedEffect(pesos) {
        animatedYs.forEachIndexed { index, animatable ->
            launch {
                animatable.animateTo(
                    targetValue = (pesos[index] - minPeso) / (maxPeso - minPeso),
                    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
                )
            }
        }
    }
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(50.dp)
    )
    {
        Box (
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {

            rendimiento?.let {
                val color = if (it >= 0) Color(0xFF27DD03) else Color(0xFFFF4C4C)
                val signo = if (it >= 0) "+" else "-"
                val texto = "$signo${kotlin.math.abs(it).toInt()}%    "

                Row(
                    modifier = Modifier
                        .offset(x = 120.dp, y = -(115).dp)
                        .size(width = 80.dp, height = 35.dp)
                        .background(Color(0xFF2A2C38), shape = RoundedCornerShape(12.dp)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center


                ) {
                    Text(text = texto, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        modifier = Modifier.size(17.dp),
                        painter = painterResource(if(signo == "-") R.drawable.decrease else R.drawable.increase),
                        contentDescription = "icon chart",
                        tint = if (signo == "-") Color(0xFFFF4C4C) else Color(0xFF27DD03)
                    )

                }
            }

            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(true) {
                        detectTapGestures { offset ->
                            val spacing = size.width / (pesos.size - 1)
                            val points = animatedYs.mapIndexed { index, animY ->
                                Offset(
                                    x = (index * spacing).toFloat(),
                                    y = size.height - animY.value * size.height
                                )
                            }
                            points.forEachIndexed { index, point ->
                                if ((offset - point).getDistance() < 30f) {
                                    touchedIndex = index
                                    return@detectTapGestures
                                }
                            }
                            touchedIndex = -1
                        }
                    }
            ) {
                val spacing = size.width / (pesos.size - 1)
                val points = animatedYs.mapIndexed { index, animY ->
                    Offset(
                        x = (index * spacing),
                        y = size.height - animY.value * size.height
                    )
                }
                // Curva suavizada
                val curvedPath = Path().apply {
                    if (points.isNotEmpty()) {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            val prev = points[i - 1]
                            val curr = points[i]
                            val midX = (prev.x + curr.x) / 2
                            cubicTo(
                                midX, prev.y,
                                midX, curr.y,
                                curr.x, curr.y
                            )
                        }
                    }
                }

                // Copia la misma curva y añade la base para formar el área rellena
                val fillPath = Path().apply {
                    addPath(curvedPath) // añade la misma curva
                    lineTo(points.last().x, size.height) // baja al fondo
                    lineTo(points.first().x, size.height) // traza la base
                    close() // cierra el área
                }

                // Relleno debajo de la curva
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF3A86FF).copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )



                drawLine(Color.Gray, Offset(0f, 0f), Offset(0f, size.height), strokeWidth = 4f)
                drawLine(Color.Gray, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 4f)

                // Eje Y
                val ySteps = 5
                for (i in 0..ySteps) {
                    val y = size.height - (i / ySteps.toFloat()) * size.height - 10
                    val pesoLabel = minPeso + (i / ySteps.toFloat()) * (maxPeso - minPeso)
                    drawContext.canvas.nativeCanvas.drawText(
                        "${pesoLabel.toInt()} KG",
                        15f,
                        y,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 30f
                        }
                    )
                }


                points.forEach {
                    drawCircle(Color(0xFF27DD03), center = it, radius = 8f)
                }
                val path = Path().apply {
                    if (points.isNotEmpty()) {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            val prev = points[i - 1]
                            val curr = points[i]
                            val controlPointX = (prev.x + curr.x) / 2
                            cubicTo(
                                controlPointX, prev.y,
                                controlPointX, curr.y,
                                curr.x, curr.y
                            )
                        }
                    }
                }
                drawPath(
                    path = path,
                    color = Color(0xFF27DD03),
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )

                // Tooltip
                if (touchedIndex in points.indices) {
                    val point = points[touchedIndex]
                    val registro = filteredHistory[touchedIndex]

                    val tooltipLines = listOf(
                        "Fecha: ${registro.fecha}",
                        "Peso: ${registro.peso} kg",
                        "Reps: ${registro.repeticiones}",
                        "RM: ${registro.rm}"
                    )

                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 32f
                    }

                    val textPadding = 20f
                    val textHeight = 40f
                    val tooltipWidth = tooltipLines.maxOf { paint.measureText(it) } + textPadding * 2
                    val tooltipHeight = tooltipLines.size * textHeight + textPadding + 5

                    var tooltipX = point.x + 10f
                    val tooltipY = (point.y - tooltipHeight).coerceAtLeast(0f)
                    if (tooltipX + tooltipWidth > size.width) {
                        tooltipX = point.x - tooltipWidth - 10f
                    }

                    drawRoundRect(
                        color = darkDetailColor.copy(alpha = 0.8f),
                        topLeft = Offset(tooltipX, tooltipY),
                        size = androidx.compose.ui.geometry.Size(tooltipWidth, tooltipHeight),
                        cornerRadius = CornerRadius(12f, 12f)
                    )

                    drawIntoCanvas { canvas ->
                        tooltipLines.forEachIndexed { i, line ->
                            canvas.nativeCanvas.drawText(
                                line,
                                tooltipX + textPadding,
                                tooltipY + textPadding + (i + 1) * textHeight - 10f,
                                paint
                            )
                        }
                    }
                }
            }

        }
    }
}