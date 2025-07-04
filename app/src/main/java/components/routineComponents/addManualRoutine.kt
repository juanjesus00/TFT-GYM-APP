package components.routineComponents

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import firebase.auth.AuthRepository
import kotlinx.coroutines.launch
import model.DiaRutina
import model.DiaRutinaManual
import model.DiaRutinaUI
import model.Ejercicio
import model.EjercicioManual
import model.EjercicioUI
import routes.NavigationActions

@Composable
fun AddRoutineDialog(
    authRepository: AuthRepository = viewModel(),
    isVisible: Boolean,
    tipo: String,
    ejercicio: String,
    navigationActions: NavigationActions,
    navController: NavController,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val dias = remember { mutableStateListOf(DiaRutinaUI(mutableStateOf("Dia 1"))) }
    val scrollState = rememberScrollState()
    if(isVisible){
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {                     // ------ BOTÓN GUARDAR ------
                Button(
                    onClick = {
                        // 1. Convertir UI -> data‑classes
                        val diaRutinaList = dias.map { diaUI ->
                            DiaRutinaManual(
                                dia = diaUI.dia.value,
                                ejercicios = diaUI.ejercicios.map { exUI ->
                                    EjercicioManual(
                                        nombre = exUI.nombre.value,
                                        reps   = exUI.reps.value,
                                        series = exUI.series.value.toIntOrNull() ?: 0
                                    )
                                }
                            )
                        }
                        // 2. Guardar en Firestore
                        coroutineScope.launch {
                            val mapRutina = convertirRutinaAMap(diaRutinaList)
                            authRepository.saveManualRoutine(tipo = tipo, ejercicio = ejercicio, contenido = mapRutina)
                            onDismiss()
                            navigationActions.navigateToRoutineSelector()
                        }

                    }
                ) { Text("Guardar") }
            },
            dismissButton = { Button(onClick = onDismiss) { Text("Cancelar") } },
            title = { Text("Añadir rutina manual") },
            text = {
                Column(Modifier.verticalScroll(scrollState)) {
                    dias.forEachIndexed { index, diaUI ->
                        Card(
                            Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(Modifier.padding(8.dp)) {
                                // ------ CABECERA DEL DÍA ------
                                OutlinedTextField(
                                    value = diaUI.dia.value,
                                    onValueChange = { diaUI.dia.value = it },
                                    label = { Text("Día ${index + 1}") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(Modifier.height(8.dp))

                                // ------ LISTA DE EJERCICIOS ------
                                diaUI.ejercicios.forEachIndexed { exIdx, exUI ->
                                    Text("Ejercicio ${exIdx + 1}", style = MaterialTheme.typography.labelMedium)

                                    OutlinedTextField(
                                        value = exUI.nombre.value,
                                        onValueChange = { exUI.nombre.value = it },
                                        label = { Text("Nombre") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row {
                                        OutlinedTextField(
                                            value = exUI.reps.value,
                                            onValueChange = { exUI.reps.value = it },
                                            label = { Text("Reps") },
                                            keyboardOptions = KeyboardOptions.Default.copy(
                                                keyboardType = KeyboardType.Number
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 4.dp)
                                        )
                                        OutlinedTextField(
                                            value = exUI.series.value,
                                            onValueChange = { exUI.series.value = it },
                                            label = { Text("Series") },
                                            keyboardOptions = KeyboardOptions.Default.copy(
                                                keyboardType = KeyboardType.Number
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Spacer(Modifier.height(8.dp))
                                }

                                // ------ BOTÓN + EJERCICIO ------
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = { diaUI.ejercicios.add(EjercicioUI()) }
                                    ) { Icon(Icons.Default.Add, "Añadir ejercicio") }
                                    IconButton(
                                        onClick = { diaUI.ejercicios.removeAt(index) }
                                    ) { Icon(Icons.Default.Delete, "Añadir ejercicio") }
                                }
                            }
                        }
                    }

                    // ------ BOTÓN + DÍA ------
                    Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center
                    ) {
                        Button(

                            onClick = { dias.add(DiaRutinaUI(mutableStateOf("Día ${dias.size + 1}"))) }
                        ) {
                            Icon(Icons.Default.Add, "Añadir día")
                            Spacer(Modifier.width(4.dp))
                            Text("Añadir día")
                        }
                    }
                }
            }
        )
    }

}


fun convertirRutinaAMap(rutina: List<DiaRutinaManual>): List<Map<String, Any>> {
    return rutina.map { dia ->
        mapOf(
            "dia" to dia.dia,
            "ejercicios" to dia.ejercicios.map { ejercicio ->
                mapOf(
                    "nombre" to ejercicio.nombre,
                    "reps" to ejercicio.reps,
                    "series" to ejercicio.series
                )
            }
        )
    }
}
