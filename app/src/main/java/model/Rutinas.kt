package model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList

data class RutinaFirebase(
    var activa: Boolean = false,
    var fecha: String = "",
    var contenido: List<DiaRutina> = emptyList()
)

data class DiaRutina(
    var dia: String = "",
    var ejercicios: List<Ejercicio> = emptyList(),
    var hecho: Boolean = false
)

data class Ejercicio(
    var nombre: String = "",
    var reps: String = "",
    var series: Int = 0
)

data class EjercicioUI(
    val nombre: MutableState<String> = mutableStateOf(""),
    val reps: MutableState<String> = mutableStateOf(""),
    val series: MutableState<String> = mutableStateOf("")
)

data class DiaRutinaUI(
    var dia: MutableState<String> = mutableStateOf(""),
    val ejercicios: SnapshotStateList<EjercicioUI> = mutableStateListOf(EjercicioUI())
)

data class DiaRutinaManual(
    var dia: String,
    var ejercicios: List<EjercicioManual> = emptyList()
)

data class EjercicioManual(
    var nombre: String,
    var reps: String,
    var series: Int
)