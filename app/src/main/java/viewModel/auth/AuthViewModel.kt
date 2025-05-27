package viewModel.auth

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import firebase.auth.AuthRepository
import routes.NavigationActions

class  AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel(){
    private var loginState by mutableStateOf("")

    fun login(email: String, password: String, context: Context, onSuccess: () -> Unit, navigationActions: NavigationActions, onErrorAction: () -> Unit) {
        repository.signIn(email, password, context = context, onSuccess = onSuccess, navigationActions = navigationActions, onErrorAction)
    }

    fun register(
        email: String,
        password: String,
        context: Context,
        name: String,
        onSuccess: () -> Unit,
        navigationActions: NavigationActions,
        repeatPassword: String
    ){
        repository.register(email, password, context, name, onSuccess, navigationActions = navigationActions, repeatPassword = repeatPassword)
    }

    fun editUser(gender: String, birthDate: String, weight: Int, height: Int, onSuccess: () -> Unit){
        repository.editUser(gender, birthDate, weight, height, onSuccess)
    }

    fun logOut(onSuccess: () -> Unit){
        repository.logOut(onSuccess)
    }

    fun editUserImageStorage(uriImage: Uri?, onSuccess: () -> Unit){
        repository.editUserImageStorage(uriImage = uriImage, onSuccess = onSuccess)
    }

}