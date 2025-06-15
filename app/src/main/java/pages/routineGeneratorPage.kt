package pages
import android.util.Log
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tft_gym_app.R
import components.buttons.GetDefaultIconButton
import components.inputs.GetInputWithDropdown
import components.langSwitcher.getStringByName
import firebase.auth.AuthRepository


@Composable
fun RutinaGeneradorScreen(
    scrollState: ScrollState,
    navigationActions: NavigationActions,
    navController: NavController,
    gymViewModel: GymViewModel,
    authRepository: AuthRepository = viewModel()
) {
    val context = LocalContext.current
    val geminiApiService = remember { GeminiApiService(context) }
    val coroutineScope = rememberCoroutineScope()

    var resultado by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var prompt by remember { mutableStateOf("genera una rutina de entrenamiento para ") }
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

    val benchPressRoutine = listOf("Smolov Jr. Bench", "Sheiko", "Bench Press Specialization (Greg Nuckols)", "Westside Barbell (DE/ME method)", "5/3/1 (Jim Wendler)", "Bilbo Method")
    var expandedBPR by remember { mutableStateOf(false) }
    var selectedBPR by remember { mutableStateOf("") }

    val deadLiftRoutine = listOf("Candito 6 Week Program", "Coan/Phillipi Deadlift", "Texas Method (Deadlift Variant)", "Sheiko (Deadlift focused)", "5/3/1 Deadlift")
    var expandedDLR by remember { mutableStateOf(false) }
    var selectedDLR by remember { mutableStateOf("") }

    val squadRoutine = listOf("Smolov (Full)", "Russian Squat Routine", "Texas Method (Squat Focused)", "Shako (Squat oriented)", "5x5 Stronglifts / Madcow")
    var expandedSR by remember { mutableStateOf(false) }
    var selectedSR by remember { mutableStateOf("") }

    val hypertrophyRoutine = listOf("Push Pull Legs (PPL)","Full Body (Cuerpo completo)","Upper/Lower Split","Bro Split (Weider)", "Hypertrophy-Specific Training (HST)", "heavyDuty (Mike Mentzer)")
    var expandedHR by remember { mutableStateOf(false) }
    var selectedHR by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 120.dp)
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
            GetInputWithDropdown(
                expanded = expandedHR,
                selectedText = selectedHR,
                onExpanded = {expandedHR = it},
                onSelectedText = {selectedHR = it},
                onDismissExpanded = {expandedHR = false},
                options = hypertrophyRoutine
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

            GetInputWithDropdown(
                expanded = expandedExercise,
                selectedText = selectedExercise,
                onExpanded = {expandedExercise = it},
                onSelectedText = {selectedExercise = it},
                onDismissExpanded = {expandedExercise = false},
                options = exerciseOptions
            )
            when(true){
                (selectedExercise == "Press de Banca" || selectedExercise == "Bench Press") -> {
                    GetInputWithDropdown(
                        expanded = expandedBPR,
                        selectedText = selectedBPR,
                        onExpanded = {expandedBPR = it},
                        onSelectedText = {selectedBPR = it},
                        onDismissExpanded = {expandedBPR = false},
                        options = benchPressRoutine
                    )
                }
                (selectedExercise == "Peso Muerto" || selectedExercise == "Deadlift") -> {
                    GetInputWithDropdown(
                        expanded = expandedDLR,
                        selectedText = selectedDLR,
                        onExpanded = {expandedDLR = it},
                        onSelectedText = {selectedDLR = it},
                        onDismissExpanded = {expandedDLR = false},
                        options = deadLiftRoutine
                    )
                }
                (selectedExercise == "Sentadilla" || selectedExercise == "Squad") -> {
                    GetInputWithDropdown(
                        expanded = expandedSR,
                        selectedText = selectedSR,
                        onExpanded = {expandedSR = it},
                        onSelectedText = {selectedSR = it},
                        onDismissExpanded = {expandedSR = false},
                        options = squadRoutine
                    )
                }
                else -> {
                    // Acción por defecto
                }
            }



        }

        GetInputWithDropdown(
            expanded = expandedLevel,
            selectedText = selectedLevel,
            onExpanded = {expandedLevel = it},
            onSelectedText = {selectedLevel = it},
            onDismissExpanded = {expandedLevel = false},
            options = userLevelOptions
        )

        GetDefaultIconButton(
            text = "Generar Rutina",
            onClick = {
                if (selectedRoutine.isEmpty() || selectedLevel.isEmpty()) {
                    error = getStringByName(context, "complete_fields") ?: "Complete all fields"
                    return@GetDefaultIconButton
                }

                val promptBuilder = StringBuilder().apply {
                    append("Genera una rutina de entrenamiento para $selectedRoutine ")

                    when (selectedRoutine) {
                        "Hypertrophy", "Hipertrofia" -> {
                            append("de $selectedTimeHypertrophy días, ")
                            append("usando el programa $selectedHR. ")
                        }
                        "Strength", "Fuerza" -> {
                            append("de $selectedTimeStrength semanas, ")
                            append("especializada en $selectedExercise ")
                            append("con el método ${getSelectedRoutineName(selectedExercise, selectedBPR, selectedDLR, selectedSR)}. ")
                        }
                    }

                    append("Nivel: $selectedLevel. ")
                    append("Formato requerido: \n")
                    append("- SOLO incluir listas por días en formato MARKDOWN\n")
                    append("- Cada día con encabezado ## Día [número/nombre]\n")
                    append("- Tabla de ejercicios con columnas: Ejercicio, Series, Repeticiones\n")
                    append("- Sin explicaciones adicionales, solo la rutina")
                }

                prompt = promptBuilder.toString()
                Log.d("PromptOptimizado", prompt)

                cargando = true
                error = null
                resultado = ""

                coroutineScope.launch {
                    try {
                        val result = geminiApiService.sendPrompt(prompt)
                        result.onSuccess { textoLimpio ->
                            resultado = textoLimpio
                            authRepository.saveRoutine(selectedRoutine, selectedExercise, textoLimpio)
                        }.onFailure { e ->
                            error = "Error: ${e.message}"
                            Log.e("routinePage", "Error capturado: $e")
                        }
                    } catch (e: Exception) {
                        error = "Error: ${e.message}"
                        Log.e("routinePage", "Error capturado: $e")
                    }
                    cargando = false
                }
            },
            enabled = !cargando,
            icon = R.drawable.aiicon
        )


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

    }


}

private fun getSelectedRoutineName(selectedExercise:String, selectedBPR: String, selectedDLR: String, selectedSR: String): String {
    return when (selectedExercise) {
        "Press de Banca" -> selectedBPR
        "Peso Muerto" -> selectedDLR
        "Sentadilla" -> selectedSR
        else -> ""
    }
}

// Clase auxiliar para resultados
sealed class Result<out T> {
    data class Success<out T>(val value: T) : Result<T>()
    data class Failure(val exception: Exception) : Result<Nothing>()
}


