package components.buttons

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import components.langSwitcher.getStringByName

@Composable
fun GetDefaultButton(text: String, onClick:() -> Unit, enabled: Boolean){
    Button(
        onClick = {
            onClick.invoke()
        },
        enabled = enabled
    ) {
        Text(text = text)
    }
}