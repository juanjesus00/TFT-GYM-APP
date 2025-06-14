package geminiApi
import android.content.Context
import androidx.annotation.WorkerThread
import com.example.tft_gym_app.R
import geminiApi.Content
import geminiApi.GeminiRequest
import geminiApi.Part
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import android.util.Log


class GeminiApiService(private val context: Context) {

    // Configuración JSON robusta
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    // Cliente HTTP con timeouts
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS) // 3 minutos para respuestas largas
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun sendPrompt(prompt: String): Result<String> {
        return try {
            withContext(Dispatchers.IO) {
                val apiKey = context.getString(R.string.gemini_api_key)
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"

                val requestBody = json.encodeToString(
                    GeminiRequest(
                        contents = listOf(
                            Content(
                                parts = listOf(Part(text = prompt))
                            )
                        )
                    )
                )

                Log.d("GeminiAPI", "Request: $requestBody")

                val request = Request.Builder()
                    .url(url)
                    .post(requestBody.toRequestBody("application/json".toMediaType()))
                    .build()

                val response: Response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: ""
                    Log.e("GeminiAPI", "Error ${response.code}: $errorBody")
                    return@withContext Result.failure(
                        Exception("HTTP ${response.code}: ${response.message}")
                    )
                }

                val responseBody = response.body?.string() ?: ""
                Log.d("GeminiAPI", "Response: $responseBody")

                val geminiResponse = json.decodeFromString<GeminiResponse>(responseBody)

                geminiResponse.error?.let { error ->
                    return@withContext Result.failure(
                        Exception("API Error: ${error.message}")
                    )
                }

                val text = geminiResponse.candidates?.firstOrNull()
                    ?.content?.parts?.firstOrNull()?.text

                if (!text.isNullOrEmpty()) {
                    Result.success(text)
                } else {
                    Result.failure(Exception("Respuesta vacía"))
                }
            }
        } catch (e: Exception) {
            Log.e("GeminiAPI", "Error: ${e.message}", e)
            Result.failure(e)
        }
    }
}