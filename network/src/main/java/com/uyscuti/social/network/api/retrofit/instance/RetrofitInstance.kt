package com.uyscuti.social.network.api.retrofit.instance

import android.content.Context
import android.util.Log
import com.uyscuti.social.network.api.retrofit.interfaces.IFlashapi
import com.uyscuti.social.network.utils.LocalStorage
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class RetrofitInstance @Inject constructor(
    private val localStorage: LocalStorage, context: Context) {

    private val BASE_URL = "http://192.168.1.103:8080/api/v1/"
    //private val BASE_URL = "http://api.flashmobile.app:8080/api/v1/"

    private fun getToken(): String {
        return localStorage.getToken() ?: ""
    }

    private val cachedDir: File? = context.cacheDir

    private val cache = cachedDir?.let { Cache(it, 10 * 1024 * 1024) }

    // Logging interceptor to see raw responses
    private val loggingInterceptor = Interceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)

        // Log the response body without consuming it
        val responseBody = response.body
        val source = responseBody?.source()
        source?.request(Long.MAX_VALUE)
        val buffer = source?.buffer

        val responseBodyString = buffer?.clone()?.readString(Charsets.UTF_8)
        Log.d("API_RESPONSE", "URL: ${request.url}")
        Log.d("API_RESPONSE", "Response Code: ${response.code}")
        Log.d("API_RESPONSE", "Response Body: $responseBodyString")

        response
    }

    private val httpClient = OkHttpClient.Builder()
        .cache(cache)
        .addInterceptor(loggingInterceptor) // Add logging first
        .addInterceptor(RetryInterceptor())
        .writeTimeout(200, TimeUnit.SECONDS)
        .readTimeout(200, TimeUnit.SECONDS)
        .callTimeout(200, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val accessToken = getToken()
            if (accessToken.isNotEmpty()) {
                val requestWithAuth = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $accessToken")
                    .build()
                chain.proceed(requestWithAuth)
            } else {
                chain.proceed(originalRequest)
            }
        }
        .build()

    val apiService: IFlashapi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(IFlashapi::class.java)
    }
}

class RetryInterceptor : Interceptor {

    private var maxRetries: Int = 3
    private var retryDelayMillis: Long = 1000

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var retryCount = 0

        while (response == null || !response.isSuccessful && retryCount < maxRetries) {
            response?.close()
            retryCount++
            Thread.sleep(retryDelayMillis)
            response = chain.proceed(request)
        }

        return response ?: throw IOException("Max retries reached")
    }

}