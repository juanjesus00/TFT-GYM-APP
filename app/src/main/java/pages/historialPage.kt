package pages

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tft_gym_app.R
import com.example.tft_gym_app.ui.theme.detailsColor
import components.box.GetHistoryDayBox
import components.buttons.GetOptionButton
import components.chart.HistoryChart
import components.langSwitcher.getStringByName
import components.menu.GetEditHistoryMenu
import components.menu.GetSettingMenu
import firebase.auth.AuthRepository
import routes.NavigationActions
import viewModel.api.GymViewModel

@Composable
fun GetHistorialPage(
    scrollState: ScrollState,
    navigationActions: NavigationActions,
    navController: NavController,
    gymViewModel: GymViewModel,
    authRepository: AuthRepository = viewModel()
) {
    var expandedIndex by remember { mutableIntStateOf(-1) }
    val exercise by gymViewModel.historyExercise.observeAsState()
    val context = LocalContext.current
    var history by remember { mutableStateOf(emptyList<model.Registro>()) }
    var changeMode by remember { mutableStateOf(false) }
    var editableMenu by remember { mutableStateOf(false) }



    LaunchedEffect(Unit) {
        getStringByName(context, exercise.toString())?.let{
            authRepository.getHistoryUser( exercise = it, onResult = { value ->
                value?.let{ history = it}
            })
        }
    }

    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFFD78323), Color(0xFF27DD03)),
        start = Offset(25f, 25f),
        end = Offset(100f, 100f)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 120.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 20.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween

        ){
            getStringByName(context, exercise?:"")?.let{
                Text(
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(brush = gradient, fontSize = 20.sp, fontWeight = FontWeight.Bold)) {
                            append(it)
                        }
                    },
                )
            }

            Icon(
                painter = if(!changeMode) painterResource(R.drawable.chart) else painterResource(R.drawable.schedule),
                contentDescription = "history type",
                tint = detailsColor,
                modifier = Modifier
                    .size(30.dp)
                    .clickable(
                        onClick = {
                            changeMode = !changeMode
                        }
                    )
            )
        }

        if(changeMode){
            getStringByName(context, exercise.toString())?.let{
                HistoryChart(history = history, exerciseName = it)
            }

        }else{
            history.forEachIndexed { index, item ->
                Row (
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    GetHistoryDayBox(
                        date = item.fecha,
                        isExpanded = expandedIndex == index,
                        onToggleExpand = {
                            expandedIndex = if (expandedIndex == index) -1 else index
                        },
                        peso = "${item.peso} KG",
                        repeticiones = item.repeticiones.toString(),
                        rm = "${item.rm} KG"
                    )

                    GetSettingMenu(navigationActions, navController, index, history = history, exercise = exercise)

                }

            }

            getStringByName(context, "add")?.let{
                GetOptionButton(
                    text = it,
                    onClick = {
                        editableMenu = !editableMenu
                    }
                )
            }
            GetEditHistoryMenu(
                navigationActions = navigationActions,
                navController = navController,
                isMenuVisible = editableMenu,
                onDismiss = {editableMenu = false},
                onAccept = "add",
                index = 0,
                history = history,
                exercise = exercise
            )
        }


    }
}