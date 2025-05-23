package components.userProfileComponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import components.box.GetUserStatsBox
import firebase.auth.AuthRepository
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Composable
fun  GetUserStats (authRepository: AuthRepository = viewModel()){

    var infoUser by remember { mutableStateOf(emptyMap<String, Any>()) }

    LaunchedEffect(Unit) {
        authRepository.getInfoUser { user ->
            user?.let { infoUser = it }
        }
    }

    val edad = ageCalculator(infoUser["birthDate"].toString())

    Row (
        modifier = Modifier.fillMaxWidth().padding(start = 30.dp, end = 30.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ){

        GetUserStatsBox(text = "Peso", stat = "${infoUser["weight"]} KG")
        GetUserStatsBox(text = "Altura", stat = "${infoUser["height"]} CM")
        GetUserStatsBox(text = "Edad", stat = "$edad años")

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