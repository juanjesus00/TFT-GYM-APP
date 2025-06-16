package components.routineComponents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tft_gym_app.ui.theme.darkDetailColor
import components.buttons.GetOptionButton
import components.langSwitcher.getStringByName
import components.menu.GetEditHistoryMenu
import model.DiaRutina
import model.RutinaFirebase
import routes.NavigationActions
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun RutinaCard(
    rutina: RutinaFirebase,
    index: Int,
    initiallyExpanded: Boolean = false,
    navController: NavController,
    navigationActions: NavigationActions
) {
    val parsedDate = LocalDate.parse(rutina.fecha, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    val formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale("es", "ES"))

    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val titulo = if (rutina.activa) "Rutina activa" else "Rutina ${index + 1}: ${parsedDate.format(formatter)}"


    Card(
        modifier = Modifier
            .padding(10.dp)
            .width(340.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = darkDetailColor
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))

                rutina.contenido.forEachIndexed { diaIndex, dia ->
                    DiaRutinaItem(index = diaIndex, dia = dia)
                    Divider(color = Color.LightGray, thickness = 1.dp)
                }
            }
        }
    }

}


@Composable
fun DiaRutinaItem(index: Int, dia: DiaRutina) {
    var isChecked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isChecked, onCheckedChange = { isChecked = it })
            Text(text = dia.dia, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(4.dp))

        dia.ejercicios.forEach { ejercicio ->
            Text(
                text = "- ${ejercicio.nombre}: ${ejercicio.series} series de ${ejercicio.reps} reps",
                fontSize = 13.sp
            )
        }
    }
}
