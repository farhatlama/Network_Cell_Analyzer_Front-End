package utils

import api.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl
import java.io.IOException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://network-cell-analyzer-backend-production.up.railway.app/"

    // Retrofit API service for POST/GET with models
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    // OkHttp client for generic GET requests with headers and query params
    private val client = OkHttpClient()

    suspend fun getWithParams(
        endpoint: String,
        headers: Map<String, String>,
        params: Map<String, String>
    ): String? = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = HttpUrl.parse(BASE_URL + endpoint.removePrefix("/"))?.newBuilder()
                ?: return@withContext null

            for ((key, value) in params) {
                urlBuilder.addQueryParameter(key, value)
            }

            val requestBuilder = Request.Builder().url(urlBuilder.build())

            for ((key, value) in headers) {
                requestBuilder.addHeader(key, value)
            }

            val response = client.newCall(requestBuilder.build()).execute()
            if (response.isSuccessful) {
                return@withContext response.body()?.string()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return@withContext null
    }
}
