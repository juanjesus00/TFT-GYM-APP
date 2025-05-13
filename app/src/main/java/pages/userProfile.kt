package pages

import android.R
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import routes.NavigationActions

@Composable
fun GetUserProfile(
    scrollState: ScrollState,
    navigationActions: NavigationActions,
    navController: NavController
) {
    val user = Firebase.auth.currentUser

    Column (
        modifier = Modifier.fillMaxSize().padding(32.dp)
    ){
        Text(text =  "${user?.email} Profile")
    }

}