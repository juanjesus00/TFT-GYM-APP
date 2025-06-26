package viewModel.rm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RmCalculator : ViewModel() {
    private val _estimatedRm = MutableLiveData<Float>()
    val estimatedRm: LiveData<Float> = _estimatedRm

    fun analyzeRepetition(weight: Float, reps: Int) {
        //val velocity = RepetitionAnalyzer.calculateRepsPerSecond(frameCount, fps)

        val rm = RepetitionAnalyzer.estimateRmWithFatigue(weight, reps)
        _estimatedRm.value = rm

        // ampliar para mas calculos de rm

    }

    fun getLevelStrength(exercise: String, bodyWeight: Float, rm: Float, onResult: (String) -> Unit) {
        val ratio = if (bodyWeight > 0) rm / bodyWeight else 0f

        when (exercise) {
            "Press de Banca" -> {
                val levels = mapOf(
                    "Élite" to 2.00f,
                    "Avanzado" to 1.75f,
                    "Intermedio" to 1.25f,
                    "Novato" to 0.75f
                )
                onResult(determineLevel(ratio, levels, "Principiante"))
            }

            "Peso Muerto" -> {
                val levels = mapOf(
                    "Élite" to 3.00f,
                    "Avanzado" to 2.50f,
                    "Intermedio" to 2.00f,
                    "Novato" to 1.50f
                )
                onResult(determineLevel(ratio, levels, "Principiante"))
            }

            "Sentadilla" -> {
                val levels = mapOf(
                    "Élite" to 2.75f,
                    "Avanzado" to 2.25f,
                    "Intermedio" to 1.50f,
                    "Novato" to 1.25f
                )
                onResult(determineLevel(ratio, levels, "Principiante"))
            }

            else -> onResult("Nivel no definido")
        }
    }

    private fun determineLevel(
        ratio: Float,
        levels: Map<String, Float>,
        defaultLevel: String
    ): String {
        return levels.entries.firstOrNull { (_, threshold) -> ratio >= threshold }?.key ?: defaultLevel
    }
}