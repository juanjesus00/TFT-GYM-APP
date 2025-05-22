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
        navController.navigate(Routes.LOGIN)
    }

    fun navigateToRegister(){
        navController.navigate(Routes.REGISTER)
    }

    fun navigateToUserData(){
        navController.navigate(Routes.USERDATA)
    }

    fun navigateToUserProfile(){
        navController.navigate(Routes.USERPROFILE)
    }

    fun navigateToVideoUploader(){
        navController.navigate(Routes.VIDEO)
    }

    fun navigateToEditUserInfo(){
        navController.navigate(Routes.EDITUSERINFO)
    }
}