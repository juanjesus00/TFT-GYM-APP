package model

import java.util.Date

data class Registro (
    val fecha: String,
    val peso: Float,
    val repeticiones: Int,
    val rm: Float
) {
    fun toMap(): MutableMap<String, Any>{
        return mutableMapOf(
            "fecha" to this.fecha,
            "peso" to this.peso,
            "repeticiones" to this.repeticiones,
            "rm" to this.rm
        )
    }
}