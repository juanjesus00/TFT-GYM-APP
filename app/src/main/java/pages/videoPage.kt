package pages

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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
    var selectVideoUri by remember { mutableStateOf<Uri?>(null) }
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
                val body = MultipartBody.Part.createFormData("video", tempFile.name, requestFile)

                viewModel.uploadVideo(body)

            }
        }
    )
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
                .background(color = Color(0x1A000000), shape = RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ){
            Icon(
                painter = painterResource(id = R.drawable.video),
                contentDescription = "video",
                modifier = Modifier.background(color = Color(0x40000000))
            )
        }

        getStringByName(LocalContext.current, "upload_video")?.let{
            GetDefaultButton(text = it, onClick = {launcher.launch("video/*")})
        }


        GetDefaultButton(text = "resultado", onClick = {viewModel.fetchResults(viewModel.analysisId.value)})



        GetInputWithDropdown(
            expanded = expanded,
            selectedText = selectedText,
            onExpanded = {expanded = it},
            onSelectedText = {selectedText = it},
            onDismissExpanded = {expanded = false},
            options = options
        )

        GetInputLogin(
            text = "test",
            onValueChange = { },
            label = "test label",
            placeholder = "texto de prueba"
        )
    }
}