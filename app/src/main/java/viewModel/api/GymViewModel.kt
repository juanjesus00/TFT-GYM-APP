package viewModel.api

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import api.AnalyzeResponse
import api.ResultResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import network.ApiClient
import okhttp3.MultipartBody
import retrofit2.Response
import viewModel.rm.RepetitionAnalyzer
import java.time.LocalDate
import javax.inject.Inject


class GymViewModel: ViewModel(){
    private val _analyzeResponse = MutableStateFlow<String>("")
    val analyzeResponse: StateFlow<String> = _analyzeResponse

    private val _resultResponse = MutableStateFlow<ResultResponse?>(null)
    val resultResponse: StateFlow<ResultResponse?> = _resultResponse

    private val _progress = MutableStateFlow<String>("")
    val progress: StateFlow<String> = _progress

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private var pollingJob: Job? = null

    public var analysisId = mutableStateOf("")

    var selectedText by mutableStateOf("")
        private set
    var weight by mutableStateOf("")
        private set

    private val _weight = MutableLiveData<String>()
    val observeWeight: LiveData<String> = _weight
    private val _selectedText = MutableLiveData<String>()
    val observeSelectedText: LiveData<String> = _selectedText

    private val _historyExercise = MutableLiveData<String>()
    val historyExercise: LiveData<String> = _historyExercise

    var selectVideoUri by mutableStateOf<Uri?>(null)
        private set

    fun actualizarSelectedText(value: String){
        selectedText = value
        _selectedText.value = value
    }

    fun actualizarWeight(value: String){
        weight = value
        _weight.value = value
    }

    fun actualizarSelectedVideoUri(value: Uri?){
        selectVideoUri = value
    }

    fun actualizarHistoryExercise(value: String){
        _historyExercise.value = value
    }

    fun uploadVideo(
        videoPart: MultipartBody.Part
    ) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response: Response<AnalyzeResponse> = ApiClient.apiService.analyzeVideo(videoPart)
                if (response.isSuccessful) {
                    val body = response.body()
                    response.body()?.let {
                        _analyzeResponse.value = it.analysis_id
                    }


                    // Aquí accedes al ID
                    Log.d("GymViewModel", "analysis_id = ${analyzeResponse.value}")
                } else {
                    Log.e("GymViewModel", "Error: ${response.code()}")

                }
            } catch (e: Exception) {
                Log.e("GymViewModel", "Exception: ${e.localizedMessage}")
            }
        }
    }

    private fun fetchResults(id: String) {
        viewModelScope.launch {
            try {
                val response: Response<ResultResponse> = ApiClient.apiService.getResults(id)
                if (response.isSuccessful) {
                    val body = response.body()

                    if (body?.status == "completed" && body.results != null) {
                        _resultResponse.value = body
                        Log.d("GymViewModel", "results = ${resultResponse.value?.results}")
                        stopPolling()
                    } else {
                        Log.d("GymViewModel", "results = waiting for results")
                    }
                } else {
                    Log.e("GymViewModel", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

    fun startPollingProgress(id: String) {
        pollingJob?.cancel() // cancela si ya está en marcha

        pollingJob = viewModelScope.launch {
            while (true) {
                try {
                    // Obtener progreso
                    val progressResponse = ApiClient.apiService.getProgress(id)
                    if (progressResponse.isSuccessful) {
                        val progressValue = progressResponse.body()?.progress
                        _progress.value = progressValue ?: "0"

                        if (progressValue == "100") {
                            // Intentar obtener resultados
                            val resultResponse = ApiClient.apiService.getResults(id)
                            if (resultResponse.isSuccessful) {
                                val resultBody = resultResponse.body()
                                Log.d("GymViewModel", "Resultado crudo: $resultBody")

                                if (resultBody?.status == "completed" && resultBody.results != null) {
                                    _resultResponse.value = resultBody
                                    _loading.value = false
                                    stopPolling()
                                    break
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    break
                }

                delay(1000L) // espera 1 segundo
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
    }

    fun clearResponses() {
        _analyzeResponse.value = ""
        _resultResponse.value = null
        _progress.value = "0"
    }

}