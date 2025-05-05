package components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import components.menu.HamburgerMenu


@Composable
fun GetHeader(navigationActions: NavigationActions, navController: NavController){
    var isMenuVisible by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
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
                    onClick = { isMenuVisible = !isMenuVisible }
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
                .size(60.dp)
                .clip(RoundedCornerShape(30.dp))
                .clickable(onClick = {}),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(25.dp))
                ,
                painter = painterResource(id = profileimage),
                contentDescription = "header menu",
                contentScale = ContentScale.Crop
            )
        }
    }
    Box (

    ){
        HamburgerMenu(navigationActions, navController, isMenuVisible, onDismiss = { isMenuVisible = false })
    }


}