package pages

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tft_gym_app.R
import components.buttons.GetDefaultButton
import components.inputs.GetInputLogin
import components.inputs.GetInputWithDropdown
import components.langSwitcher.getStringByName
import components.progressBar.GetRoundProgressBar
import firebase.auth.AuthRepository
import okhttp3.MultipartBody
import routes.NavigationActions
import viewModel.api.GymViewModel
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import viewModel.rm.RmCalculator
import java.io.File

@Composable
fun GetVideoPage(
    scrollState: ScrollState,
    navigationActions: NavigationActions,
    navController: NavController,
    gymViewModel: GymViewModel,
    viewModelRmCalculator: RmCalculator = viewModel(),
    viewModelRepository: AuthRepository = viewModel()
) {


    val context = LocalContext.current
    val options = listOf("dead_lift", "bench_press", "squad").mapNotNull { name ->
        getStringByName(context, name)
    }
    var expanded by remember { mutableStateOf(false) }
    val selectedText = gymViewModel.selectedText //by remember { mutableStateOf("") }
    val weight = gymViewModel.weight
    val selectVideoUri = gymViewModel.selectVideoUri
    val rm by viewModelRmCalculator.estimatedRm.observeAsState()
    val currentDateTime = java.time.LocalDate.now()

    val progress by gymViewModel.progress.collectAsState()
    val progressFloat = progress.toFloatOrNull()?.div(100f) ?: 0f
    val analysis by gymViewModel.analyzeResponse.collectAsState()
    val results by gymViewModel.resultResponse.collectAsState()
    val isLoading by gymViewModel.loading.collectAsState()

    var videoBody by remember { mutableStateOf<MultipartBody.Part?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            gymViewModel.actualizarSelectedVideoUri(uri)
            uri?.let {
                val inputStream = context.contentResolver.openInputStream(uri)
                val tempFile = File(context.cacheDir, "temp_video.mp4")
                inputStream?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val requestFile = tempFile.asRequestBody("video/mp4".toMediaTypeOrNull())
                videoBody = MultipartBody.Part.createFormData("video", tempFile.name, requestFile)
            }
        }
    )

    LaunchedEffect(analysis) {
        if (analysis.isNotBlank()) {
            gymViewModel.startPollingProgress(id = analysis)
        }
    }

    var alreadySaved by remember { mutableStateOf(false) }

    LaunchedEffect(results) {
        val data = results?.results
        if (data != null && !alreadySaved) {
            try {
                viewModelRmCalculator.analyzeRepetition(
                    weight = weight.toFloat(),
                    reps = data.reps
                )

                viewModelRepository.editUserFromVideo(
                    exercise = selectedText,
                    weight = weight.toFloat(),
                    repetitions = data.reps,
                    date = currentDateTime.toString(),
                    rm = rm
                )

                alreadySaved = true // Para evitar repeticiones
            } catch (e: Exception) {
                Log.e("VideoPage", "Error al guardar en Firebase: ${e.message}")
            }
        }
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 80.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(30.dp)
    ){
        getStringByName(context, "upload_video")?.let{
            Text(text = it)
        }

        Box (
            modifier = Modifier
                .size(width = 350.dp, height = 250.dp)
                .background(color = Color(0x1A000000), shape = RoundedCornerShape(20.dp))
                .clickable(
                    onClick = {
                        launcher.launch("video/*")
                    },
                    enabled = !isLoading
                ),
            contentAlignment = Alignment.Center
        ){
            Log.d("videoPage:", "$isLoading")
            if (isLoading){
                GetRoundProgressBar(gymViewModel)
            }else if (selectVideoUri != null){
                AndroidView(
                    factory = { context ->
                        VideoView(context).apply {
                            setVideoURI(selectVideoUri)
                            val mediaController = MediaController(context)
                            mediaController.setAnchorView(this)
                            setMediaController(mediaController)
                            setOnPreparedListener { mp ->
                                mp.isLooping = false
                                seekTo(1)
                                pause()
                            }
                        }
                    },
                    modifier = Modifier
                        .size(width = 350.dp, height = 250.dp)
                        .background(Color.Black, shape = RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                )
            }else{
                Icon(
                    painter = painterResource(id = R.drawable.video),
                    contentDescription = "video",
                    modifier = Modifier.background(color = Color(0x40000000))
                )
            }

        }

        results?.results?.let {
            viewModelRmCalculator.analyzeRepetition(weight = weight.toFloat(), reps = it.reps)
            viewModelRepository.editUserFromVideo(
                exercise = selectedText,
                weight = weight.toFloat(),
                repetitions = it.reps,
                date = currentDateTime.toString(),
                rm = rm
            )
            gymViewModel.actualizarWeight("")
            gymViewModel.actualizarSelectedText("")
            gymViewModel.clearResponses()
        }

        GetInputWithDropdown(
            expanded = expanded,
            selectedText = selectedText,
            onExpanded = {expanded = it},
            onSelectedText = {gymViewModel.actualizarSelectedText(it)},
            onDismissExpanded = {expanded = false},
            options = options
        )

        GetInputLogin(
            text = weight,
            onValueChange = { gymViewModel.actualizarWeight(it)},
            label = "peso",
            placeholder = "peso"
        )

        getStringByName(LocalContext.current, "analyze")?.let{
            GetDefaultButton(text = it, onClick = {
                videoBody?.let {
                    gymViewModel.uploadVideo(it)
                    videoBody = null
                    gymViewModel.actualizarSelectedVideoUri(null)
                    alreadySaved = false
                } ?: Toast.makeText(context, "Primero selecciona un video", Toast.LENGTH_SHORT).show()
            },
                //poner condicion de enable = true si los inputs y el video han sido seleccionados
                enabled = selectedText.isNotEmpty() && weight.isNotEmpty() && selectVideoUri != null
            )
        }
    }
}