package components.registerSuggest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import components.langSwitcher.getStringByName

@Composable
fun GetRegisterSuggest() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        getStringByName(LocalContext.current, "register_suggest")?.let {
            Text(text = it, color = Color.Black)
        }
        getStringByName(LocalContext.current, "register_link")?.let {
            Text(text = it, color = Color.Blue, fontWeight = FontWeight.Bold)
        }
    }


}