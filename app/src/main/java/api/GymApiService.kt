package api

import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import retrofit2.Response

import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface GymApiService {
    @Multipart
    @POST("analyze")
    suspend fun analyzeVideo(
        @Part video: MultipartBody.Part,
        @Part("exercise") exercise: RequestBody
    ): Response<AnalyzeResponse>

    @GET("results/{id}")
    suspend fun getResults(@Path("id") id: String): Response<ResultResponse>

    @GET("progress/{id}")
    suspend fun getProgress(@Path("id") id:String): Response<ProgressResponse>
}