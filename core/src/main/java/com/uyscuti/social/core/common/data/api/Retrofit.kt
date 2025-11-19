package com.uyscuti.social.core.common.data.api

import android.content.Context
import com.uyscuti.social.network.api.retrofit.interfaces.IFlashapi
import com.uyscuti.social.network.utils.LocalStorage

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class Retrofit(context: Context?) {
    private val BASE_URL = "http://api.flashmobile.app:8080/api/v1/"
    private var token: String? = null
    private val context: Context = context!!

    private fun getToken(): String {
        return LocalStorage.getInstance(context).getToken()
    }

    private  val httpClient = OkHttpClient.Builder().addInterceptor( Interceptor { chain ->
        val originalRequest = chain.request()
        val accessToken = getToken()
        if (accessToken.length > 10 ){
            val requestWithAuth = originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
            chain.proceed(requestWithAuth)
        } else {
            chain.proceed(originalRequest)
        }
    }).build()

    val regService: IFlashapi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(IFlashapi::class.java)
    }
}
