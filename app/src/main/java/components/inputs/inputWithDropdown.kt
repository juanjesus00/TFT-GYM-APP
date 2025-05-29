package components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import components.langSwitcher.getStringByName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GetInputWithDropdown(
    expanded: Boolean,
    selectedText: String,
    onSelectedText: (String) -> Unit,
    onExpanded: (Boolean) -> Unit,
    onDismissExpanded: () -> Unit,
    options: List<String>,
) {


    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { onExpanded(!expanded) },
        modifier = Modifier
            .width(280.dp)
    ) {
        OutlinedTextField(

            value = selectedText,
            onValueChange = { onSelectedText(it) },
            label = { Text("Selecciona una opción") },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true, // Solo permite elegir del dropdown
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                onDismissExpanded.invoke()
                               },
            modifier = Modifier
                .background(Color(0xFF161818), shape = RoundedCornerShape(bottomEnd = 20.dp, bottomStart = 20.dp)),
            shape = RoundedCornerShape(bottomEnd = 20.dp, bottomStart = 20.dp)
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onSelectedText(selectionOption)
                        onDismissExpanded.invoke()
                    }
                )
            }
        }
    }
}
