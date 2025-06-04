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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.*
import com.example.tft_gym_app.ui.theme.darkDetailColor
import com.example.tft_gym_app.ui.theme.detailsColor
import components.inputs.GetInputWithDropdown
import kotlinx.coroutines.launch
import model.Registro
import java.text.SimpleDateFormat
import java.util.*


enum class TimeFilter(val label: String) {
    LAST_7_DAYS("Últimos 7 días"),
    LAST_MONTH("Último mes"),
    LAST_YEAR("Último año"),
    ALL("Todos")
}

enum class ChartType(val label: String) {
    LINE("Línea"),
    BAR("Barras")
}

@Composable
fun HistoryChart(
    history: List<Registro>,
    exerciseName: String
) {
    if (history.isEmpty()) {
        Text("No hay datos disponibles para $exerciseName")
        return
    }
    val options = listOf(TimeFilter.LAST_7_DAYS.toString(), TimeFilter.LAST_MONTH.toString(), TimeFilter.LAST_YEAR.toString(), TimeFilter.ALL.toString())
    var expanded by remember { mutableStateOf(false) }
    val formatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val now = remember { Date() }

    var selectedFilter by remember { mutableStateOf(TimeFilter.ALL) }
    var chartType by remember { mutableStateOf(ChartType.LINE) }

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

    if (filteredHistory.isEmpty()) {
        Text("No hay datos en el periodo seleccionado.")
        return
    }

    val pesos = filteredHistory.map { it.rm.toFloat() }
    val fechas = filteredHistory.map { it.fecha }

    val maxPeso = (pesos.maxOrNull() ?: 1f) + 5
    val minPeso = pesos.minOrNull() ?: 0f

    var touchedIndex by remember { mutableIntStateOf(-1) }

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
            .padding(top = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(50.dp)
    )
    {
        Box (
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
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

                drawLine(Color.Gray, Offset(0f, 0f), Offset(0f, size.height), strokeWidth = 4f)
                drawLine(Color.Gray, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 4f)

                // Eje Y
                val ySteps = 10
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

                // Eje X
                val labelEvery = (fechas.size / 4).coerceAtLeast(1)
                fechas.forEachIndexed { index, fecha ->
                    if (index % labelEvery == 0) {
                        val x = index * spacing + 10
                        drawContext.canvas.nativeCanvas.drawText(
                            fecha,
                            x,
                            size.height + 55f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.GRAY
                                textSize = 30f
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                    }
                }

                // Gráfico: Línea o Barras
                if (chartType == ChartType.LINE) {
                    for (i in 0 until points.size - 1) {
                        drawLine(detailsColor, points[i], points[i + 1], strokeWidth = 4f)
                    }
                    points.forEach {
                        drawCircle(Color(0xFF27DD03), center = it, radius = 8f)
                    }
                } else if (chartType == ChartType.BAR) {
                    val barWidth = spacing * 0.6f
                    points.forEach { point ->
                        drawRect(
                            color = detailsColor,
                            topLeft = Offset(point.x - barWidth / 2, point.y),
                            size = androidx.compose.ui.geometry.Size(barWidth, size.height - point.y)
                        )
                    }
                }

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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                GetInputWithDropdown(
                    expanded = expanded,
                    selectedText = selectedFilter.toString(),
                    onExpanded = {expanded = it},
                    onSelectedText = {selectedFilter = TimeFilter.valueOf(it.toString())},
                    onDismissExpanded = {expanded = false},
                    options = options
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ChartType.values().forEach { type ->
                    Text(
                        text = type.label,
                        modifier = Modifier
                            .padding(4.dp)
                            .background(if (chartType == type) Color.Gray else detailsColor, shape = RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .clickable { chartType = type }
                    )
                }
            }


        }
    }
}