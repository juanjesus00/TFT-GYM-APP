package components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tft_gym_app.R
import com.example.tft_gym_app.R.drawable.profileimage
import routes.NavigationActions

@Composable
fun GetHeader(navigationActions: NavigationActions, navController: NavController){

    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, start = 30.dp, end = 30.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(25.dp))
                .clickable(
                    onClick = {}
                )
        ){
            Image(
                painter = painterResource(id = R.drawable.jam_menu),
                contentDescription = "header menu",
                contentScale = ContentScale.Crop
            )
        }

        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(Color(0xFFFFFFFF))
                .clickable(
                    onClick = {}
                )
        ){
            Image(
                painter = painterResource(id = profileimage),
                contentDescription = "header menu",
                contentScale = ContentScale.Crop
            )
        }
    }

}