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

}