package pages

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import routes.NavigationActions
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tft_gym_app.ui.theme.backgroundColor
import components.buttons.GetDefaultButton
import components.userProfileComponents.GetProfileImage
import viewModel.auth.AuthViewModel

@Composable
fun GetEditUserInfo(
    scrollState: ScrollState,
    navigationActions: NavigationActions,
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri : Uri? ->
            selectImageUri = uri
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted){
            launcher.launch("image/*")
        }else{
            Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }

    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(color = backgroundColor)
            .verticalScroll(scrollState)
    ){
        GetProfileImage(
            selectImageUri = selectImageUri,
            onEditClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(READ_MEDIA_IMAGES)
                        } else {
                            permissionLauncher.launch(READ_EXTERNAL_STORAGE)
                        } },
            imageSize = 75,
            editIconSize = 24
        )

        Box (
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ){
            GetDefaultButton(text = "Aceptar", enabled = true, onClick = {authViewModel.editUserImageStorage(uriImage = selectImageUri, onSuccess = {navigationActions.navigateToHome()})})
        }


    }


}