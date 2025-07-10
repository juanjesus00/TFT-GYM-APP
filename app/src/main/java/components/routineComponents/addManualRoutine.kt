package components.routineComponents

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tft_gym_app.ui.theme.backgroundColor
import com.example.tft_gym_app.ui.theme.darkDetailColor
import com.example.tft_gym_app.ui.theme.detailsColor
import com.example.tft_gym_app.ui.theme.labelColor
import firebase.auth.AuthRepository
import kotlinx.coroutines.launch
import model.DiaRutina
import model.DiaRutinaManual
import model.DiaRutinaUI
import model.Ejercicio
import model.EjercicioManual
import model.EjercicioUI
import model.RutinaFirebase
import routes.NavigationActions

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun AddRoutineDialog(
    authRepository: AuthRepository = viewModel(),
    isVisible: Boolean,
    tipo: String,
    ejercicio: String,
    index: Int = 0,
    routine: List<RutinaFirebase> = listOf(),
    navigationActions: NavigationActions,
    navController: NavController,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var dias = remember { mutableStateListOf(DiaRutinaUI(mutableStateOf("Dia 1"))) }
    val scrollState = rememberScrollState()
    if(isVisible){
        if(routine.isNotEmpty()) {
            dias.clear()
            dias.addAll(convertirDiaRutinaAUI(routine[index].contenido))
        }
        AlertDialog(
            modifier = Modifier.fillMaxWidth(),
            onDismissRequest = onDismiss,
            confirmButton = {
                Button(
                    onClick = {
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
                            authRepository.saveManualRoutine(tipo = tipo, ejercicio = ejercicio, contenido = mapRutina, index = if (routine.isNotEmpty()) index else null)

                            onDismiss()
                            navigationActions.navigateToRoutineSelector()
                        }

                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = detailsColor,
                        contentColor = Color.White
                    )
                ) { Text("Guardar") }
            },
            containerColor = backgroundColor,
            dismissButton = { Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(
                containerColor = detailsColor,
                contentColor = Color.White
            )) { Text("Cancelar") } },
            title = { if (routine.isNotEmpty())  Text("Editar rutina") else Text("Añadir rutina manual")},
            text = {
                Column(Modifier.verticalScroll(scrollState).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(15.dp)) {

                    dias.forEachIndexed { i, diaUI ->
                        Card(
                            Modifier
                                .fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = darkDetailColor
                            )
                        ) {
                            Column(Modifier.padding(8.dp)) {

                                OutlinedTextField(
                                    value = diaUI.dia.value,
                                    onValueChange = { diaUI.dia.value = it },
                                    label = { Text("Día ${i + 1}") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedLabelColor = detailsColor,
                                        focusedIndicatorColor = detailsColor,
                                        unfocusedContainerColor = darkDetailColor,
                                        unfocusedLabelColor = labelColor,
                                        unfocusedIndicatorColor = labelColor
                                    )
                                )

                                Spacer(Modifier.height(8.dp))

                                diaUI.ejercicios.forEachIndexed { exIdx, exUI ->
                                    Text("Ejercicio ${exIdx + 1}", style = MaterialTheme.typography.labelMedium)

                                    OutlinedTextField(
                                        value = exUI.nombre.value,
                                        onValueChange = { exUI.nombre.value = it },
                                        label = { Text("Nombre") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = TextFieldDefaults.colors(
                                            focusedLabelColor = detailsColor,
                                            focusedIndicatorColor = detailsColor,
                                            unfocusedContainerColor = darkDetailColor,
                                            unfocusedLabelColor = labelColor,
                                            unfocusedIndicatorColor = labelColor
                                        )
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
                                                .padding(end = 4.dp),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = TextFieldDefaults.colors(
                                                focusedLabelColor = detailsColor,
                                                focusedIndicatorColor = detailsColor,
                                                unfocusedContainerColor = darkDetailColor,
                                                unfocusedLabelColor = labelColor,
                                                unfocusedIndicatorColor = labelColor
                                            )
                                        )
                                        OutlinedTextField(
                                            value = exUI.series.value,
                                            onValueChange = { exUI.series.value = it },
                                            label = { Text("Series") },
                                            keyboardOptions = KeyboardOptions.Default.copy(
                                                keyboardType = KeyboardType.Number
                                            ),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = TextFieldDefaults.colors(
                                                focusedLabelColor = detailsColor,
                                                focusedIndicatorColor = detailsColor,
                                                unfocusedContainerColor = darkDetailColor,
                                                unfocusedLabelColor = labelColor,
                                                unfocusedIndicatorColor = labelColor
                                            )
                                        )
                                    }

                                    Spacer(Modifier.height(8.dp))
                                }

                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = { diaUI.ejercicios.add(EjercicioUI()) }
                                    ) { Icon(Icons.Default.Add, "Añadir ejercicio") }
                                    IconButton(
                                        enabled = diaUI.ejercicios.size > 1,
                                        onClick = { diaUI.ejercicios.removeLast() }
                                    ) { Icon(Icons.Default.Delete, "Eliminar ejercicio") }
                                }
                            }
                        }
                    }

                    Row(
                        Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(

                            onClick = { dias.add(DiaRutinaUI(mutableStateOf("Día ${dias.size + 1}"))) },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = detailsColor
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Añadir día", modifier = Modifier.size(35.dp))
                        }

                        IconButton(
                            enabled = dias.size > 1, // evitar que se borre el último día
                            onClick = {
                                dias.removeAt(dias.lastIndex)
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = detailsColor
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar último día", modifier = Modifier.size(35.dp))
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


fun convertirDiaRutinaAUI(lista: List<DiaRutina>): SnapshotStateList<DiaRutinaUI> {
    val resultado = mutableStateListOf<DiaRutinaUI>()

    lista.forEach { dia ->
        val ejerciciosUI = mutableStateListOf<EjercicioUI>()
        dia.ejercicios.forEach { ejercicio ->
            ejerciciosUI.add(
                EjercicioUI(
                    nombre = mutableStateOf(ejercicio.nombre),
                    reps = mutableStateOf(ejercicio.reps),
                    series = mutableStateOf(ejercicio.series.toString())
                )
            )
        }
        resultado.add(
            DiaRutinaUI(
                dia = mutableStateOf(dia.dia),
                ejercicios = ejerciciosUI
            )
        )

    }

    return resultado
}