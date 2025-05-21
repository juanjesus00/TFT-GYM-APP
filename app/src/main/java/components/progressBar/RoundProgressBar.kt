package components.progressBar

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import viewModel.api.GymViewModel

@Composable
fun GetRoundProgressBar(viewModel: GymViewModel = viewModel()){

    val progress by viewModel.progress.collectAsState()
    val progressFloat = progress.toFloatOrNull()?.div(100f) ?: 0f

    CircularProgressIndicator(
        progress = { progressFloat },
        color = Color(0xFFD78323),
        strokeWidth = 4.dp,
        modifier = Modifier.size(64.dp)
    )
}