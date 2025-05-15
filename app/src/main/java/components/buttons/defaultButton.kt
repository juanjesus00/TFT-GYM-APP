package components.buttons

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import components.langSwitcher.getStringByName

@Composable
fun GetDefaultButton(text: String, onClick:() -> Unit){
    Button(
        onClick = {
            onClick.invoke()
        }
    ) {
        Text(text = text)
    }
}