package components.userProfileComponents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import components.buttons.GetDefaultButton
import components.langSwitcher.getStringByName

@Composable
fun GetDeleteUserUi(){
    Box (
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ){
        getStringByName(LocalContext.current, "delete_user")?.let{
            GetDefaultButton(text = it, onClick = {}, enabled = true)
        }
    }
}