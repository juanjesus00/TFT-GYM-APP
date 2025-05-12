package components.checkBoxs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun GetRegisterCheckBox(isChecked: Boolean, onValueChange: (Boolean) -> Unit){
    Row (
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    )
    {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onValueChange
        )
        Text(modifier = Modifier.width(250.dp), text = "By continuing you accept our Privacy Policy and Term of Use", color = Color(0xFFACA3A5), fontSize = 12.sp)
    }
}