package viewModel.auth

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import firebase.auth.AuthRepository
import routes.NavigationActions

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel(){
    private var loginState by mutableStateOf("")

    fun login(email: String, password: String, context: Context, onSuccess: () -> Unit, navigationActions: NavigationActions, onErrorAction: () -> Unit) {
        repository.signIn(email, password, context = context, onSuccess = onSuccess, navigationActions = navigationActions, onErrorAction)
    }

    fun register(email: String, password: String) {
        repository.register(email, password) { success, error ->
            loginState = if (success) "Registered" else "Error: $error"
        }
    }
}