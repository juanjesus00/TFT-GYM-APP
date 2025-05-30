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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import components.inputs.GetInputLogin
import components.langSwitcher.getStringByName
import components.userProfileComponents.GetProfileImage
import firebase.auth.AuthRepository
import viewModel.api.GymViewModel
import kotlin.String

@Composable
fun GetEditUserInfo(
    scrollState: ScrollState,
    navigationActions: NavigationActions,
    navController: NavController,
    gymViewModel: GymViewModel,
    authRepository: AuthRepository = viewModel()
) {
    val context = LocalContext.current

    var selectImageUri by remember { mutableStateOf<Uri?>(null) }
    var userName by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var birthdate by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var infoUser by remember { mutableStateOf(emptyMap<String, Any>()) }


    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri : Uri? ->
            selectImageUri = uri
        }
    )

    val photoUri = remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ){ success ->
        if (success) {
            selectImageUri = photoUri.value
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted){
            galleryLauncher.launch("image/*")
        }else{
            Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }

    }

    LaunchedEffect(Unit) {
        authRepository.getInfoUser { user ->
            user?.let { infoUser = it }
            userName = user?.get("userName").toString()
            weight = user?.get("weight").toString()
            height = user?.get("height").toString()
            birthdate = user?.get("birthDate").toString()
            gender = user?.get("gender").toString()
        }
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(color = backgroundColor)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
            editIconSize = 24,
            startPadding = 60,
            topPadding = 110
        )

        getStringByName(context, "user_name")?.let { label ->
            GetInputLogin(
                text = userName,
                onValueChange = { userName = it },
                label = label,
                placeholder = label
            )
        }
        getStringByName(context, "weight")?.let { label ->
            GetInputLogin(
                text = weight,
                onValueChange = { weight = it },
                label = "${label}-kg",
                placeholder = label
            )
        }
        getStringByName(context, "height")?.let { label ->
            GetInputLogin(
                text = height,
                onValueChange = { height = it },
                label = "${label}-cm",
                placeholder = label
            )
        }

        getStringByName(context, "birthdate")?.let { label ->
            GetInputLogin(
                text = birthdate,
                onValueChange = { birthdate = it },
                label = label,
                placeholder = label
            )
        }

        getStringByName(context, "gender")?.let { label ->
            GetInputLogin(
                text = gender,
                onValueChange = { gender = it },
                label = label,
                placeholder = label
            )
        }

        Box (
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center

        ){
            getStringByName(context, "save_changes")?.let{ label ->
                GetDefaultButton(text = label, enabled = true, onClick = {
                    authRepository.editUser2(
                        userInfoToUpdate = model.User(
                            email = infoUser["email"].toString(),
                            userId = infoUser["user_id"].toString(),
                            userName = userName,
                            profileImageUrl = infoUser["profileImageUrl"].toString(),
                            gender = gender,
                            birthDate = birthdate,
                            weight = weight.toInt(),
                            height = height.toInt()
                        ),
                        onSuccess = {
                            if(selectImageUri != null ) authRepository.editUserImageStorage(uriImage = selectImageUri, onSuccess = {navigationActions.navigateToHome()}) else navigationActions.navigateToHome()
                        }
                    )
                })
            }

        }


    }


}