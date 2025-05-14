package pages

import com.example.tft_gym_app.R
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tft_gym_app.R.drawable.profileimage
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import components.userProfileComponents.GetProfileImage
import components.userProfileComponents.GetUserPersonalRecords
import components.userProfileComponents.GetUserStats
import routes.NavigationActions

@Composable
fun GetUserProfile(
    scrollState: ScrollState,
    navigationActions: NavigationActions,
    navController: NavController
) {
    val user = Firebase.auth.currentUser

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFF2A2C38))
            .verticalScroll(state = scrollState),
        verticalArrangement = Arrangement.spacedBy(30.dp)
    ){
        GetProfileImage()
        GetUserStats()
        GetUserPersonalRecords()

    }

}