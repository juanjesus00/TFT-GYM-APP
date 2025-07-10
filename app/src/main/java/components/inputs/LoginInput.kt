package components.inputs

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.unit.dp
import com.example.tft_gym_app.ui.theme.darkDetailColor
import com.example.tft_gym_app.ui.theme.detailsColor
import com.example.tft_gym_app.ui.theme.labelColor

@Composable
fun GetInputLogin(text: String, onValueChange: (String) -> Unit, label: String, placeholder: String) {
    OutlinedTextField(
        value = text,
        onValueChange = onValueChange,
        label = { Text( text = label) },
        placeholder = { Text(text = placeholder) },
        shape = RoundedCornerShape(20.dp),
        colors = TextFieldDefaults.colors(
            focusedLabelColor = detailsColor,
            focusedIndicatorColor = detailsColor,
            unfocusedContainerColor = darkDetailColor,
            unfocusedLabelColor = labelColor,
            unfocusedIndicatorColor = labelColor
        )
    )
}