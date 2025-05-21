package network

import api.GymApiService
import firebase.auth.FirebaseAuthInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.jvm.java

object ApiClient {
    private const val BASE_URL = "https://gymapi.org/"

    private val client = OkHttpClient.Builder()
        .addInterceptor(FirebaseAuthInterceptor())
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: GymApiService by lazy {
        retrofit.create(GymApiService::class.java)
    }
}