package pages

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import components.buttons.GetOptionButton
import components.langSwitcher.getStringByName
import components.menu.GetSettingMenu
import components.routineComponents.RutinaCard
import routes.NavigationActions
import viewModel.api.GymViewModel

@Composable
fun GetRoutinePage(
    scrollState: ScrollState,
    navigationActions: NavigationActions,
    navController: NavController,
    gymViewModel: GymViewModel
) {
    val routineType by gymViewModel.routineType.observeAsState()
    val context = LocalContext.current
    var editableMenu by remember { mutableStateOf(false) }
    LaunchedEffect(routineType) {
        val type = getStringByName(context, routineType ?: "")
        if (!type.isNullOrBlank()) {
            gymViewModel.cargarRutinas(tipo = type)
        }
    }
    val routineHypertrophy  = gymViewModel.rutinasHipertrofia
    val routineStrength  = gymViewModel.rutinasFuerza

    Column(
        modifier = Modifier.fillMaxSize().padding(top = 120.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(routineHypertrophy.isNotEmpty()){
            routineHypertrophy.forEachIndexed { index, rutina ->
                Row (
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),

                ){
                    RutinaCard(rutina = rutina, index = index, initiallyExpanded = rutina.activa, navController, navigationActions)
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .padding(top = 8.dp),
                        contentAlignment = Alignment.Center,
                    )
                    {
                        GetSettingMenu(navigationActions, navController, index, routine = routineHypertrophy, exercise = routineType)
                    }

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
        }else if(routineStrength.isNotEmpty()){
            /*routineStrength.forEachIndexed { index, rutina ->
                RutinaCard(rutina = rutina, index = index, initiallyExpanded = rutina.activa, navController, navigationActions)
            }
            getStringByName(context, "add")?.let{
                GetOptionButton(
                    text = it,
                    onClick = {
                        editableMenu = !editableMenu
                    }
                )
            }*/
        }

    }

}