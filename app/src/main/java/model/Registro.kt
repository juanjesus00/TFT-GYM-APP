package model

import java.util.Date

data class Registro (
    var fecha: String,
    var peso: Float,
    var repeticiones: Int,
    var rm: Float
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