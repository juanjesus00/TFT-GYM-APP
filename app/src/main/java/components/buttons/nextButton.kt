package components.buttons

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import components.langSwitcher.getStringByName

@Composable
fun GetNextButton(buttonText: String, onNextButton: () -> Unit){
    Button(
        modifier = Modifier,
        onClick = { onNextButton.invoke()}
    ) {
        getStringByName(LocalContext.current, buttonText)?.let {
            Text( text = it )
        }
    }
}