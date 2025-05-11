package firebase.auth

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import routes.NavigationActions
import android.widget.Toast

class AuthRepository : ViewModel(){
    private val auth = FirebaseAuth.getInstance()

    private var _loading = MutableLiveData(false)

    private val _isEmailVerified = MutableLiveData<Boolean>()
    val isEmailVerified: LiveData<Boolean> = _isEmailVerified
    fun signIn(
        email: String,
        password: String,
        context: Context,
        onSuccess: () -> Unit,
        navigationActions: NavigationActions,
        onErrorAction: () -> Unit
    ) = viewModelScope.launch {
        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            Toast.makeText(
                context,
                "Todos los campos son obligatorios",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            try {
                //navigationActions.navigateToCarga()
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            if (FirebaseAuth.getInstance().currentUser?.isEmailVerified == false) {
                                sendVerificationRegisterEmail()
                            }
                            Log.d("Loginbackend", "Inicio de sesión exitoso")
                            onSuccess()

                        } else {
                            val errorMessage =
                                task.exception?.localizedMessage ?: "Error desconocido"
                            Log.d("LoginBackend", "Error al iniciar sesión: $errorMessage")
                            onErrorAction()
                            Toast.makeText(
                                context,
                                "Inicio de sesión fallido: $errorMessage",
                                Toast.LENGTH_LONG
                            ).show()

                        }
                    }
                    .addOnFailureListener { exception ->
                        exception.printStackTrace()
                        onErrorAction()
                        Toast.makeText(
                            context,
                            "Inicio de se sesio erroneo la contraseña o el email son incorrectos",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } catch (e: Exception) {
                Log.e("LoginBackend", "Excepción capturada: ${e.message}")
                Toast.makeText(
                    context,
                    "Ha ocurrido un error inesperado: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    private fun sendVerificationRegisterEmail() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                println("Correo de verificación enviado.")
            } else {
                println("Error al enviar el correo: ${task.exception?.message}")
            }
        }
    }
    fun checkIfEmailVerified(){
        val user = FirebaseAuth.getInstance().currentUser
        if (user?.isEmailVerified == true) {
            // El correo ha sido verificado
            _isEmailVerified.value = user.isEmailVerified
        } else {
            // El correo no ha sido verificado aún
            Log.d("Verification", "Email is not verified.")
            _isEmailVerified.value = false
        }
    }
    fun register(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }
}