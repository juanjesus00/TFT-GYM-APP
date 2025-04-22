package components.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.navigation.NavController
import com.example.tft_gym_app.R
import routes.NavigationActions

@Composable
fun HamburgerMenu(
    navigationActions: NavigationActions,
    navController: NavController,
    isMenuVisible: Boolean,
    onDismiss: () -> Unit
){
    Box (modifier = Modifier
        .offset(x = 10.dp, y = 90.dp)
    ){
        DropdownMenu (
            expanded = isMenuVisible,
            onDismissRequest = onDismiss,
            modifier = Modifier
                .size(width = 150.dp, height = 210.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF161818), shape = RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp)
        ){
            DropdownMenuItem(
                onClick = {},
                text = {Text(text = stringResource( R.string.my_data), color = Color(0xFFD78323))}
            )
            DropdownMenuItem(
                onClick = {},
                text = {Text(text = stringResource( R.string.favoriote_videos), color = Color(0xFFD78323))}
            )
            DropdownMenuItem(
                onClick = {},
                text = {Text(text = stringResource( R.string.routines), color = Color(0xFFD78323))}
            )
            DropdownMenuItem(
                onClick = {},
                text = {Text(text = stringResource( R.string.record), color = Color(0xFFD78323))}
            )

        }
    }


}