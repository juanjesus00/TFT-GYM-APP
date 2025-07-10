package components.userProfileComponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import components.box.GetUserStatsBox
import components.box.GetUserStatsBoxSkeleton
import components.langSwitcher.getStringByName
import firebase.auth.AuthRepository
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Composable
fun GetUserStats(authRepository: AuthRepository = viewModel()) {

    var infoUser by remember { mutableStateOf(emptyMap<String, Any>()) }

    LaunchedEffect(Unit) {
        authRepository.getInfoUser { user ->
            user?.let {
                infoUser = it
            }
        }
    }

    val weight = infoUser["weight"]?.toString()?.toFloatOrNull()
    val height = infoUser["height"]?.toString()?.toFloatOrNull()
    val birthDate = infoUser["birthDate"]?.toString().orEmpty()
    val edad = ageCalculator(birthDate)

    val pesoCargado = weight != null && weight > 0
    val alturaCargada = height != null && height > 0
    val edadCargada = edad != null && birthDate.isNotBlank()

    // Mostrar mensaje si el perfil está incompleto
    if (!pesoCargado || !alturaCargada || !edadCargada) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            getStringByName(LocalContext.current, "CompletePrifile")?.let {
                Text(text = it)
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 30.dp, end = 30.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (pesoCargado)
            GetUserStatsBox(text = "Peso", stat = "${weight.toInt()} KG")
        else
            GetUserStatsBoxSkeleton()

        if (alturaCargada)
            GetUserStatsBox(text = "Altura", stat = "${height.toInt()} CM")
        else
            GetUserStatsBoxSkeleton()

        if (edadCargada)
            GetUserStatsBox(text = "Edad", stat = "$edad años")
        else
            GetUserStatsBoxSkeleton()
    }
}

fun ageCalculator(birthDate: String): Int? {
    return try {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val fecha = LocalDate.parse(birthDate, formatter)
        val hoy = LocalDate.now()
        Period.between(fecha, hoy).years
    } catch (e: DateTimeParseException) {
        e.printStackTrace()
        null // o puedes retornar -1 si prefieres un entero
    }
}