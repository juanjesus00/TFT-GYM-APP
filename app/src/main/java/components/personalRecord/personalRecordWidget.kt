package components.personalRecord

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tft_gym_app.R
import components.newbox.ViewModelBox
import firebase.auth.AuthRepository
import kotlinx.coroutines.launch
import model.Widget
import kotlin.math.log

@Composable

fun GetPersonalRecordWidget(widget: Widget, listExercise: Map<String, Float>, listLevelRare: Map<String, Map<String, Any>>, viewModelBox: ViewModelBox = viewModel(), authRepository: AuthRepository = viewModel()){

    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFFD78323), Color(0xFF27DD03)),
        start = Offset(25f, 25f),
        end = Offset(100f, 100f)
    )

    val weight = listExercise[widget.exercise]
    val context = LocalContext.current
    var levelRare = 100 - ((listLevelRare[widget.exercise]?.get("rareza") ?: "100").toString()).toFloat()


    //val rango = viewModelBox.rango.value // Aquí lo obtienes correctamente
    val rango = when (widget.exercise.lowercase()) {
        "press de banca", "bench press" -> {
            when (weight?.toInt()) {
                in 90..99 -> 1
                in 100..109 -> 2
                in 110..119 -> 3
                in 120..129 -> 4
                in 130..139 -> 5
                in 140..149 -> 6
                in 150..159 -> 7
                in 160..169 -> 8
                in 170..179 -> 9
                in 180..189 -> 10
                in 190..Int.MAX_VALUE -> 11
                else -> null
            }
        }
        "peso muerto", "dead lift" -> {
            when (weight?.toInt()) {
                in 120..159 -> 1
                in 160..179 -> 2
                in 180..199 -> 3
                in 200..219 -> 4
                in 220..239 -> 5
                in 240..259 -> 6
                in 260..279 -> 7
                in 280..299 -> 8
                in 300..319 -> 9
                in 320..339 -> 10
                in 340..Int.MAX_VALUE -> 11
                else -> null
            }
        }
        "sentadilla", "squad" -> {
            when (weight?.toInt()) {
                in 120..139 -> 1
                in 140..159 -> 2
                in 160..179 -> 3
                in 180..199 -> 4
                in 200..219 -> 5
                in 220..239 -> 6
                in 240..259 -> 7
                in 260..279 -> 8
                in 280..299 -> 9
                in 300..319 -> 10
                in 320..Int.MAX_VALUE -> 11
                else -> null
            }
        }
        // Puedes agregar otros ejercicios con otros rangos
        else -> null
    }

    val iconResId = rango?.let { resId ->
        context.resources.getIdentifier("rank$resId", "drawable", context.packageName)
    }


    val animatedWeight = remember { Animatable(0f) }

    LaunchedEffect(weight) {
        weight?.let {
            animatedWeight.animateTo(
                targetValue = it,
                animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing)
            )
        }
    }

    val animatedWeightText = if (animatedWeight.value % 1.0f == 0f) {
        "${animatedWeight.value.toInt()} KG"
    } else {
        String.format("%.2f KG", animatedWeight.value)
    }



    val alpha = remember { Animatable(0f) }

    LaunchedEffect(iconResId) {

        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
            )
        }
    }

    Column (
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp),
            textAlign = TextAlign.Center,
            text = "Record personal en ${widget.exercise}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Row (
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                buildAnnotatedString {
                withStyle(style = SpanStyle(brush = gradient, fontSize = 35.sp, fontWeight = FontWeight.Bold)) {
                    append(animatedWeightText)
                }
            })


            iconResId?.let { resId ->
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = "Rango $rango",
                    modifier = Modifier
                        .size(100.dp)
                        .alpha(alpha.value)
                )
            }

        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            textAlign = TextAlign.Center,
            text = "Superior al ${levelRare}% de los levantadores",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}