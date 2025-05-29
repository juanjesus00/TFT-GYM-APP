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
}