package viewModel.rm

object RepetitionAnalyzer {
    // Fórmulas de estimación de RM

    fun estimateRmEpley(weight: Float, reps: Int): Float {
        return weight * (1 + reps / 30f)
    }

    fun estimateRmBrzycki(weight: Float, reps: Int): Float {
        return weight * (36f / (37 - reps))
    }

    fun estimateRmWithVelocity(weight: Float, velocity: Float, threshold: Float = 0.15f): Float {
        // Formula aproximada usando velocidad de repetición
        val percentOfRm = (velocity - threshold) / (1 - threshold) // simplificada
        return weight / percentOfRm
    }

    // Velocidad media (reps/seg)
    fun calculateRepsPerSecond(frameCount: Int, fps: Int): Float {
        val seconds = frameCount.toFloat() / fps
        return 1 / seconds
    }

    // Tiempo por repetición
    fun calculateTimePerRep(frameCount: Int, fps: Int): Float {
        return frameCount.toFloat() / fps
    }

    fun estimateRmWithFatigue(weight: Float, reps: Int, lossRate: Float = 0.03f): Float {
        // Basado en un modelo simple donde 1 rep más equivale a pérdida del 3%

        val estimatedRm = if(reps > 1 ) ((weight * reps)*lossRate)+weight else weight
        return estimatedRm
    }

}