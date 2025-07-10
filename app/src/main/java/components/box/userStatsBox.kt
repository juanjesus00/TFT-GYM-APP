package components.box

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun GetUserStatsBox(text:String, stat: String){
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFFD78323), Color(0xFF27DD03)),
        start = Offset(25f, 25f),
        end = Offset(100f, 100f)
    )
    Box (
        modifier = Modifier
            .size(100.dp)
            .background(
                color = Color(0xFF161818),
                shape = RoundedCornerShape(15.dp)
            )
    ){
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 5.dp, bottom = 5.dp),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(text = text, fontWeight = FontWeight.Bold)
            Text(
                buildAnnotatedString {
                    withStyle(style = SpanStyle(brush = gradient, fontSize = 20.sp, fontWeight = FontWeight.Bold)) {
                        append(stat)
                    }
                }
            )
        }

    }
}
