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

    Column(
        modifier = Modifier
            .padding(16.dp, top = 100.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Campo para personalizar el prompt
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("Describe tu rutina deseada") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Ej: Rutina para ganar masa muscular, 4 semanas, método 5x5") }
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
