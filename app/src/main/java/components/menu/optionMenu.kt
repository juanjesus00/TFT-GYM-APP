package components.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import components.buttons.GetOptionButton
import components.langSwitcher.getStringByName

@Composable
fun GetOptionMenu(
    isMenuVisible: Boolean,
    onDismiss: () -> Unit,
    onClickAction1: () -> Unit,
    actionText1: String,
    onClickAction2: () -> Unit,
    actionText2: String,
    exercise: String? = "",
    onClickAction3: () -> Unit
){
    var isChecked by remember { mutableStateOf(false) }

    if(isMenuVisible){
        Dialog(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier
                    .size(width = 300.dp, height = 200.dp)
                    .background(color = Color(0xFF161818), shape = RoundedCornerShape(20.dp)),
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                getStringByName(LocalContext.current, actionText1)?.let{
                    GetOptionButton(
                        text = it,
                        onClick = {
                            onClickAction1.invoke()
                        }
                    )
                }
                getStringByName(LocalContext.current, actionText2)?.let{
                    GetOptionButton(
                        text = it,
                        onClick = {
                            onClickAction2.invoke()
                        }
                    )
                }

                if(exercise == "hypertrophy" || exercise == "strength"){
                    getStringByName(LocalContext.current, "activateRoutine")?.let{ label ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isChecked, onCheckedChange = {
                                isChecked = it
                                onClickAction3.invoke()
                            })
                            Text(text = label, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

            }
        }
    }
}
