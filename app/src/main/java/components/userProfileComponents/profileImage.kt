package components.userProfileComponents

import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.tft_gym_app.R
import com.example.tft_gym_app.R.drawable.profileimage
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import firebase.auth.AuthRepository

@Composable
fun GetProfileImage(selectImageUri: Uri?, onEditClick: ()-> Unit, imageSize: Int, editIconSize: Int, startPadding: Int, topPadding: Int, authViewModel: AuthRepository = viewModel()){
    val context = LocalContext.current
    val currentUser = authViewModel.currentUser
    val isVerified by authViewModel.isEmailVerified.observeAsState(false)
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFFD78323), Color(0xFF27DD03)),
        start = Offset(25f, 25f),
        end = Offset(100f, 100f)
    )

    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var userName by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        authViewModel.checkIfEmailVerified()


    }
    LaunchedEffect(currentUser, isVerified) {
        if (currentUser != null && isVerified) {
            authViewModel.getInfoUser { url ->
                profileImageUrl = url?.get("profileImageUrl") as? String
                userName = url?.get("userName") as String
            }
        }
    }


    Row (
        modifier = Modifier.fillMaxWidth().padding(top = topPadding.dp, start = startPadding.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(30.dp)

    ){
        Box(
            modifier = Modifier.size(imageSize.dp)
        ) {
            selectImageUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(context)
                            .data(it)
                            .build()
                    ),
                    contentDescription = "header menu",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(imageSize.dp)
                        .clip(RoundedCornerShape((imageSize/2).dp))
                )
            } ?:
                if(currentUser == null || !isVerified || profileImageUrl == null){
                    Image(
                        painter = painterResource(id = profileimage),
                        contentDescription = "header menu",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(imageSize.dp)
                            .clip(RoundedCornerShape((imageSize/2).dp))
                    )
                }else{
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = "user profile",
                        modifier = Modifier
                            .size(imageSize.dp)
                            .clip(RoundedCornerShape((imageSize/2).dp)),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = profileimage)
                    )
                }

            Icon(
                painter = painterResource(id = R.drawable.editar),
                contentDescription = "EditProfile",
                tint = Color(0xFFD78323),
                modifier = Modifier
                    .size(editIconSize.dp)
                    .align(Alignment.BottomEnd)
                    .clickable {
                        onEditClick.invoke()
                    }
            )
        }
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(brush = gradient, fontSize = 30.sp, fontWeight = FontWeight.Bold)) {
                    append(userName)
                }
            }
        )
    }
}