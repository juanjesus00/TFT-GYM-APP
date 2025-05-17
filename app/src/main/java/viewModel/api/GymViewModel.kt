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
import api.ResultResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import network.ApiClient
import okhttp3.MultipartBody
import retrofit2.Response

class GymViewModel : ViewModel(){
    private val _analyzeResponse = MutableStateFlow<AnalyzeResponse?>(null)
    val analyzeResponse: StateFlow<AnalyzeResponse?> = _analyzeResponse

    private val _resultResponse = MutableStateFlow<ResultResponse?>(null)
    val resultResponse: StateFlow<ResultResponse?> = _resultResponse
    public var analysisId = mutableStateOf("")
    fun uploadVideo(videoPart: MultipartBody.Part) {
        viewModelScope.launch {
            try {
                val response: Response<AnalyzeResponse> = ApiClient.apiService.analyzeVideo(videoPart)
                if (response.isSuccessful) {
                    val body = response.body()
                    _analyzeResponse.value = body

                    // Aquí accedes al ID
                    analysisId.value = body?.analysis_id.toString()
                    Log.d("GymViewModel", "analysis_id = $analysisId")
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
                val response: Response<ResultResponse> = ApiClient.apiService.getResults(id)
                if (response.isSuccessful) {
                    val body = response.body()
                    _resultResponse.value = body

                    val results = body?.results
                    val duration = results?.duration
                    val reps = results?.reps
                    val outputUrl = results?.output_url
                    Log.d("GymViewModel", "results = $results, $duration, $reps, $outputUrl")
                }else{
                    Log.e("GymViewModel", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}