package components.routineComponents

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import components.langSwitcher.getStringByName
import firebase.auth.AuthRepository
import model.RutinaFirebase
import model.Widget
import routes.NavigationActions

@Composable
fun GetHroutineActiveWidget(
    widget: Widget,
    hipertrophyRoutineList: List<RutinaFirebase>,
    authRepository: AuthRepository,
    navigationActions: NavigationActions
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    Column (
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(start = 15.dp, top = 5.dp, end = 5.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ){
        Text(text = "Rutina de ${widget.exercise}")
        hipertrophyRoutineList.forEachIndexed { index, rutina ->
            if (rutina.activa) {
                val firstNotDoneIndex = rutina.contenido.indexOfFirst { !it.hecho }
                if (firstNotDoneIndex != -1) {
                    val dia = rutina.contenido[firstNotDoneIndex]
                    DiaRutinaItem(
                        rutinaIndex = index,
                        diaIndex = firstNotDoneIndex,
                        dia = dia,
                        routineActive = rutina.activa,
                        authRepository = authRepository,
                        routineType = getStringByName(context, "hypertrophy"),
                        onAction = { navigationActions.navigateToHome() }
                    )
                }
            }

        }
    }

}