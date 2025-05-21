package viewModel.api

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import api.AnalyzeResponse
import api.ProgressResponse
import api.ResultResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import network.ApiClient
import okhttp3.MultipartBody
import retrofit2.Response

class GymViewModel : ViewModel(){
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
    fun uploadVideo(videoPart: MultipartBody.Part) {
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

    fun fetchResults(id: String) {
        viewModelScope.launch {
            try {
                _loading.value = false
                val response: Response<ResultResponse> = ApiClient.apiService.getResults(id)
                if (response.isSuccessful) {
                    val body = response.body()

                    _resultResponse.value = body

                    val results = body?.results
                    val duration = results?.reps_durations
                    val reps = results?.reps
                    Log.d("GymViewModel", "results = $results, $duration, $reps")
                }else{
                    Log.e("GymViewModel", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startPollingProgress(id: String) {
        pollingJob?.cancel() // cancela si ya está en marcha

        pollingJob = viewModelScope.launch {
            while (true) {
                try {
                    val response = ApiClient.apiService.getProgress(id)
                    if (response.isSuccessful) {
                        response.body()?.let {
                            _progress.value = it.progress

                            if (it.progress == "100") {
                                stopPolling()

                                fetchResults(id)
                                return@launch
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    break // salir si falla la red
                }

                delay(1000L) // espera 1 segundo
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
    }
}