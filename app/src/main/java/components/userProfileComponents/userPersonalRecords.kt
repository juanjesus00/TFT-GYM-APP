package components.userProfileComponents
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import components.box.GetUserPersonalRecordBox
import components.buttons.GetArrowButton
import components.langSwitcher.getStringByName
import firebase.auth.AuthRepository

@Composable
fun GetUserPersonalRecords(
    authRepository: AuthRepository = viewModel()
) {
    var expanded by remember { mutableStateOf(false) }
    var listExercise by remember { mutableStateOf(emptyMap<String, Float>()) }
    var listLevelRare by remember { mutableStateOf(emptyMap<String, Map<String, Any>>()) }

    LaunchedEffect(Unit) {
        authRepository.getRMUser(onResult = { value ->
            value?.let{ listExercise = it }
        })
        Log.d("Personal Records", "$listExercise")

        authRepository.getLevelRare { value ->
            value?.let{
                listLevelRare = it
            }
        }

    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 30.dp),
        verticalArrangement = Arrangement.spacedBy(30.dp)
    ){
        Row (
            modifier = Modifier.fillMaxWidth()
                .padding(end = 30.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            getStringByName(LocalContext.current, "personalRecords")?.let{
                Text(text = it, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            GetArrowButton(isExpanded = expanded, onchange = {expanded = !expanded})
        }
        listExercise.onEachIndexed{ index, exercise ->
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(animationSpec = tween(delayMillis = index * 100)) + slideInVertically(
                    animationSpec = tween(delayMillis = index * 100),
                    initialOffsetY = { it / 2 }
                ),
                exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(
                    animationSpec = tween(300),
                    targetOffsetY = { it / 2 }
                )
            ) {
                val levelRare = listLevelRare[exercise.key]
                GetUserPersonalRecordBox(exercise.key, exercise.value, listLevelRare = levelRare)
            }

        }

    }
}