package pages



import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tft_gym_app.R
import routes.NavigationActions
import androidx.compose.ui.layout.ContentScale.Companion.Crop
import androidx.lifecycle.viewmodel.compose.viewModel
import components.menu.CascadingPopup
import components.newbox.GetBox
import components.newbox.ViewModelBox

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GetPrincipalMidSection(
    scrollState: ScrollState,
    navigationActions: NavigationActions,
    navController: NavController,
    viewModel: ViewModelBox = viewModel()
){
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2A2C38))
            .verticalScroll(scrollState)
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ){
        FlowRow (
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(40.dp)

        ){
            for (i in 0 until viewModel.boxCount) components.box.Box()
            GetBox()
        }
    }
}


