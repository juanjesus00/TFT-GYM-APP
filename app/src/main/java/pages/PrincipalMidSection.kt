package pages



import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import routes.NavigationActions
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import components.box.Box
import components.menu.GetOptionBoxMenu
import components.newbox.GetBox
import components.newbox.UnloggedGetBox
import components.newbox.ViewModelBox
import firebase.auth.AuthRepository
import viewModel.api.GymViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GetPrincipalMidSection(
    scrollState: ScrollState,
    navigationActions: NavigationActions,
    navController: NavController,
    gymViewModel: GymViewModel,
    viewModel: ViewModelBox = viewModel()
){
    var isMenuVisible by remember { mutableStateOf(false) }
    var index by remember { mutableIntStateOf(0) }
    val widgetList = viewModel.widgetList

    LaunchedEffect(Unit) {
        viewModel.loadWidgets()
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2A2C38))
            .verticalScroll(scrollState)
            .padding(top = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ){



        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp),
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ){
            widgetList.forEach { widget ->
                Box(widget = widget)
            }

            if(FirebaseAuth.getInstance().currentUser != null) GetBox(onIconClick = {selectedIndex ->
                index = selectedIndex
                isMenuVisible = true
            }
            ) else UnloggedGetBox()

        }
    }
    GetOptionBoxMenu(index, isMenuVisible = isMenuVisible, onDismiss = {isMenuVisible = false})



}


