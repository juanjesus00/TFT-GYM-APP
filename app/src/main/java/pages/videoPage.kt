package pages

import android.net.Uri
import android.util.Log
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tft_gym_app.R
import components.buttons.GetDefaultButton
import components.inputs.GetInputLogin
import components.inputs.GetInputWithDropdown
import components.langSwitcher.getStringByName
import okhttp3.MultipartBody
import routes.NavigationActions
import viewModel.api.GymViewModel
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File

@Composable
fun GetVideoPage(
    scrollState: ScrollState,
    navigationActions: NavigationActions,
    navController: NavController,
    viewModel: GymViewModel = viewModel()
) {


    val context = LocalContext.current
    val options = listOf("dead_lift", "bench_press", "squad").mapNotNull { name ->
        getStringByName(context, name)
    }
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var selectVideoUri by remember { mutableStateOf<Uri?>(null) }
    var enabled by remember { mutableStateOf(true) }

    val progress by viewModel.progress.collectAsState()
    val progressFloat = progress.toFloatOrNull()?.div(100f) ?: 0f
    val analysis by viewModel.analyzeResponse.collectAsState()
    val results by viewModel.resultResponse.collectAsState()
    val isLoading by viewModel.loading.collectAsState()

    var videoBody by remember { mutableStateOf<MultipartBody.Part?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            selectVideoUri = uri
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
            viewModel.startPollingProgress(id = analysis)
        }
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(text = "Sube un video")
        Box (
            modifier = Modifier
                .size(width = 350.dp, height = 250.dp)
                .background(color = Color(0x1A000000), shape = RoundedCornerShape(20.dp))
                .clickable(
                    onClick = {
                        videoBody?.let {
                            enabled = false
                        }
                            ?:
                            launcher.launch("video/*")
                            enabled = true

                    },
                    enabled = enabled
                ),
            contentAlignment = Alignment.Center
        ){
            Log.d("videoPage:", "$isLoading")
            if (isLoading){
                CircularProgressIndicator(
                    progress = { progressFloat },
                    color = Color(0xFFD78323),
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(64.dp)
                )
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

        getStringByName(LocalContext.current, "upload_video")?.let{
            GetDefaultButton(text = it, onClick = {
                videoBody?.let {
                    viewModel.uploadVideo(it)
                    enabled = false
                    videoBody = null
                    selectVideoUri = null
                } ?: Toast.makeText(context, "Primero selecciona un video", Toast.LENGTH_SHORT).show()
            },
                enabled = enabled
            )
        }

        results?.let {
            enabled = true
            Text(text = "Repeticiones: ${it.results.reps}")
            Text(text = "Duracion: ${it.results.reps_durations}")
            Text(text = "velocidad: ${it.results.mean_speed}")
        }

        GetInputWithDropdown(
            expanded = expanded,
            selectedText = selectedText,
            onExpanded = {expanded = it},
            onSelectedText = {selectedText = it},
            onDismissExpanded = {expanded = false},
            options = options
        )

        GetInputLogin(
            text = weight,
            onValueChange = { weight = it},
            label = "peso",
            placeholder = "peso"
        )
    }
}