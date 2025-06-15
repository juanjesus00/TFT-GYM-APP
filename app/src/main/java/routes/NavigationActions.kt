package routes

import androidx.navigation.NavController

class NavigationActions (private val navController: NavController) {
    fun navigateToHome(){
        navController.navigate(Routes.HOME){
            popUpTo(Routes.HOME){ inclusive = true }
            launchSingleTop = true
        }
    }

    fun navigateToLogin(){
        navController.navigate(Routes.LOGIN){
            popUpTo(Routes.LOGIN){ inclusive = true}
            launchSingleTop = true
        }
    }

    fun navigateToRegister(){
        navController.navigate(Routes.REGISTER){
            popUpTo(Routes.REGISTER){ inclusive = true}
            launchSingleTop = true
        }
    }

    fun navigateToUserData(){
        navController.navigate(Routes.USERDATA){
            popUpTo(Routes.USERDATA){ inclusive = true}
            launchSingleTop = true
        }
    }

    fun navigateToUserProfile(){
        navController.navigate(Routes.USERPROFILE){
            popUpTo(Routes.USERPROFILE){ inclusive = true}
            launchSingleTop = true
        }
    }

    fun navigateToVideoUploader(){
        navController.navigate(Routes.VIDEO){
            popUpTo(Routes.VIDEO){ inclusive = true}
            launchSingleTop = true
        }
    }

    fun navigateToRoutinePage(){
        navController.navigate(Routes.ROUTINEPAGE){
            popUpTo(Routes.ROUTINEPAGE){ inclusive = true}
            launchSingleTop = true
        }
    }

    fun navigateToRoutineSelector(){
        navController.navigate(Routes.ROUTINESELECTOR){
            popUpTo(Routes.ROUTINESELECTOR){ inclusive = true}
            launchSingleTop = true
        }
    }

    fun navigateToHistory(){
        navController.navigate(Routes.HISTORY){
            popUpTo(Routes.HISTORY){ inclusive = true}
            launchSingleTop = true
        }
    }

    fun navigateToHistoryPage(){
        navController.navigate(Routes.HISTORYPAGE){
            popUpTo(Routes.HISTORYPAGE){ inclusive = true}
            launchSingleTop = true
        }
    }

    fun navigateToEditUserInfo(){
        navController.navigate(Routes.EDITUSERINFO)
    }
}