package components.userProfileComponents

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.tft_gym_app.R
import com.example.tft_gym_app.R.drawable.profileimage

@Composable
fun GetProfileImage(){
    Row (
        modifier = Modifier.fillMaxWidth().padding(top = 100.dp, start = 30.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(30.dp)

    ){
        Box(
            modifier = Modifier.size(150.dp)
        ) {
            Image(
                painter = painterResource(id = profileimage),
                contentDescription = "header menu",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(75.dp))
            )
            Icon(
                painter = painterResource(id = R.drawable.editar),
                contentDescription = "EditProfile",
                tint = Color(0xFFD78323),
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.BottomEnd)
                    .clickable {}
            )
        }
        Text(text = "example text")
    }
}