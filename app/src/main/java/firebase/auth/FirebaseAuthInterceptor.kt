package firebase.auth

import okhttp3.Response
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor

class FirebaseAuthInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val currentUser = FirebaseAuth.getInstance().currentUser

        // Ejecutar de forma segura fuera de coroutines
        val token = runBlocking {
            currentUser?.getIdToken(false)?.await()?.token
        }

        val request = if (token != null) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        return chain.proceed(request)
    }
}