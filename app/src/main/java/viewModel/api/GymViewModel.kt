package viewModel.api

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import api.AnalyzeResponse
import api.ResultResponse
import firebase.auth.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import model.RutinaFirebase
import network.ApiClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import viewModel.rm.RepetitionAnalyzer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject


class GymViewModel: ViewModel(){

    private val authRepository = AuthRepository()

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

    var rutinasHipertrofia by mutableStateOf<List<RutinaFirebase>>(emptyList())
    var rutinasFuerza by mutableStateOf<List<RutinaFirebase>>(emptyList())

    private val _weight = MutableLiveData<String>()
    val observeWeight: LiveData<String> = _weight
    private val _selectedText = MutableLiveData<String>()
    val observeSelectedText: LiveData<String> = _selectedText

    private val _historyExercise = MutableLiveData<String>()
    val historyExercise: LiveData<String> = _historyExercise

    private val _routineType = MutableLiveData<String>()
    val routineType: LiveData<String> = _routineType

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

    fun actualizarRoutineType(value: String){
        _routineType.value = value
    }

    fun uploadVideo(
        videoPart: MultipartBody.Part,
        exercise: String
    ) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response: Response<AnalyzeResponse> = ApiClient.apiService.analyzeVideo(videoPart, exercise = exercise.toRequestBody("text/plain".toMediaType()))
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

    fun cargarRutinas(tipo: String) {

        when (tipo) {
            "Hipertrofia" -> {
                authRepository.getRutinasHipertrofia() {
                    rutinasHipertrofia = it
                }
            }
            "Fuerza" -> {
                authRepository.getRutinasFuerza() {
                    rutinasFuerza = it
                }
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

    @SuppressLint("ObsoleteSdkInt")
    @RequiresApi(Build.VERSION_CODES.Q)
    fun downloadProcessedVideo(
        context: Context,
        analysisId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.apiService.downloadProcessedVideo(id = analysisId)
                if (response.isSuccessful) {
                    response.body()?.let { body ->

                        val fileName = "processed_$analysisId.mp4"
                        val videoBytes = body.bytes()

                        val values = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                            put(MediaStore.MediaColumns.IS_PENDING, 1)
                        }

                        val resolver = context.contentResolver
                        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)

                        if (uri != null) {
                            resolver.openOutputStream(uri)?.use { outputStream ->
                                outputStream.write(videoBytes)
                                outputStream.flush()
                            }

                            // Marcar como no pendiente para que sea visible
                            values.clear()
                            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                            resolver.update(uri, values, null, null)

                            Log.d("Download", "Video guardado correctamente en: $uri")
                            withContext(Dispatchers.Main) {
                                onSuccess()
                            }
                        } else {
                            throw IOException("No se pudo crear el archivo en MediaStore")
                        }

                    } ?: throw IOException("El cuerpo de la respuesta está vacío")
                } else {
                    throw IOException("Fallo al descargar el video: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("Download", "Error al descargar el video: ${e.message}")
                withContext(Dispatchers.Main) {
                    onError(e)
                }
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