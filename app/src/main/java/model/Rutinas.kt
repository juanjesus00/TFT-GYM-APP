package model

data class RutinaFirebase(
    var activa: Boolean = false,
    var fecha: String = "",
    var contenido: List<DiaRutina> = emptyList()
)

data class DiaRutina(
    var dia: String = "",
    var ejercicios: List<Ejercicio> = emptyList()
)

data class Ejercicio(
    var nombre: String = "",
    var reps: String = "",
    var series: Int = 0
)