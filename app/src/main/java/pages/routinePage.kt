package pages
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import geminiApi.GeminiApiService
import kotlinx.coroutines.launch
import routes.NavigationActions
import viewModel.api.GymViewModel
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import components.inputs.GetInputWithDropdown
import components.langSwitcher.getStringByName


@Composable
fun RutinaGeneradorScreen(
    scrollState: ScrollState,
    navigationActions: NavigationActions,
    navController: NavController,
    gymViewModel: GymViewModel
) {
    val context = LocalContext.current
    val geminiApiService = remember { GeminiApiService(context) }
    val coroutineScope = rememberCoroutineScope()

    var resultado by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var prompt by remember { mutableStateOf("") }
    var expandedRoutine by remember { mutableStateOf(false) }
    var expandedExercise by remember { mutableStateOf(false) }
    var expandedTime by remember { mutableStateOf(false) }
    var expandedLevel by remember { mutableStateOf(false) }

    var selectedRoutine by remember { mutableStateOf("") }
    var selectedExercise by remember { mutableStateOf("") }
    var selectedTimeStrength by remember { mutableStateOf("") }
    var selectedTimeHypertrophy by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf("") }

    val exerciseOptions = listOf("dead_lift", "bench_press", "squad").mapNotNull { name ->
        getStringByName(context, name)
    }
    val routineTypeOptions = listOf("hypertrophy", "strength").mapNotNull { name ->
        getStringByName(context, name)
    }
    val timeHypertrophyOptions =
        getStringByName(context, "days")?.let {
            listOf("2 $it", "3 $it", "4 $it", "5 $it", "6 $it", "7 $it")
        }

    val timeStrengthOptions =
        getStringByName(context, "weeks")?.let {
            listOf("3 $it", "4 $it", "5 $it", "6 $it", "7 $it", "8 $it", "9 $it", "10 $it", "11 $it", "12 $it")
        }
    val userLevelOptions = listOf("beginner", "intermediate", "advanced").mapNotNull { name ->
        getStringByName(context, name)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, top = 100.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Campo para personalizar el prompt
        GetInputWithDropdown(
            expanded = expandedRoutine,
            selectedText = selectedRoutine,
            onExpanded = {expandedRoutine = it},
            onSelectedText = {selectedRoutine = it},
            onDismissExpanded = {expandedRoutine = false},
            options = routineTypeOptions
        )

        if (selectedRoutine == "Hypertrophy" || selectedRoutine == "Hipertrofia" ) {
            GetInputWithDropdown(
                expanded = expandedTime,
                selectedText = selectedTimeHypertrophy,
                onExpanded = {expandedTime = it},
                onSelectedText = {selectedTimeHypertrophy = it},
                onDismissExpanded = {expandedTime = false},
                options = timeHypertrophyOptions?:listOf("")
            )
        }else if (selectedRoutine == "Strength" || selectedRoutine == "Fuerza" ){
            GetInputWithDropdown(
                expanded = expandedTime,
                selectedText = selectedTimeStrength,
                onExpanded = {expandedTime = it},
                onSelectedText = {selectedTimeStrength = it},
                onDismissExpanded = {expandedTime = false},
                options = timeStrengthOptions?:listOf("")
            )
        }


        GetInputWithDropdown(
            expanded = expandedExercise,
            selectedText = selectedExercise,
            onExpanded = {expandedExercise = it},
            onSelectedText = {selectedExercise = it},
            onDismissExpanded = {expandedExercise = false},
            options = exerciseOptions
        )
        GetInputWithDropdown(
            expanded = expandedLevel,
            selectedText = selectedLevel,
            onExpanded = {expandedLevel = it},
            onSelectedText = {selectedLevel = it},
            onDismissExpanded = {expandedLevel = false},
            options = userLevelOptions
        )

        Button(
            onClick = {
                if (prompt.isEmpty()) {
                    error = "Por favor ingresa una descripción"
                    return@Button
                }

                cargando = true
                error = null
                resultado = ""

                coroutineScope.launch {
                    when (val result = geminiApiService.sendPrompt(prompt)) {
                        is Result.Success<*> -> {
                            resultado = result.value.toString()
                        }
                        is Result.Failure -> {
                            error = result.exception.message
                        }
                    }
                    cargando = false
                }
            },
            enabled = !cargando,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Generar Rutina")
        }

        // Estado de carga
        if (cargando) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Generando rutina...")
            }
        }

        // Mostrar errores
        error?.let {
            Text(
                text = "Error: $it",
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Mostrar resultados
        if (resultado.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Tu Rutina Personalizada:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = resultado)
                }
            }
        }
    }
}

// Clase auxiliar para resultados
sealed class Result<out T> {
    data class Success<out T>(val value: T) : Result<T>()
    data class Failure(val exception: Exception) : Result<Nothing>()
}
