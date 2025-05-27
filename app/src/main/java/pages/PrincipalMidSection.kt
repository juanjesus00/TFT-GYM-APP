package pages



import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import routes.NavigationActions
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import components.newbox.GetBox
import components.newbox.UnloggedGetBox
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
            if(FirebaseAuth.getInstance().currentUser != null) GetBox() else UnloggedGetBox()
        }
    }
}


