package model

data class RutinaFirebase(
    val activa: Boolean = false,
    val fecha: String = "",
    val contenido: List<DiaRutina> = emptyList()
)

data class DiaRutina(
    val dia: String = "",
    val ejercicios: List<Ejercicio> = emptyList()
)

data class Ejercicio(
    val nombre: String = "",
    val reps: String = "",
    val series: Int = 0
)