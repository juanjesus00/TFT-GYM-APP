package components.box

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import components.newbox.ViewModelBox


@Composable
fun Box(viewModel: ViewModelBox = viewModel()){
    Column (
        modifier = Modifier
            .size(width = 150.dp, height = 250.dp)
            .background(color = Color(0xFF161818), shape = RoundedCornerShape(20.dp))
    ){
        Text(text = "${viewModel.boxCount}")
    }

}