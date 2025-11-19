package com.uyscuti.social.business.retro

//import com.uyscuti.social.business.MyApp

//object RetrofitClient {
//    private const val BASE_URL = "http://192.168.1.103:8080/api/v1/"
//
//    private const val PREFS_FILENAME = "LoginPrefs"
//    private const val TOKEN_KEY = "token"
//
//    var sharedPreferences: SharedPreferences = App.getAppContext().getSharedPreferences(
//        PREFS_FILENAME, Context.MODE_PRIVATE)
//
//    private val cachedDir: File? = MyApp.getAppContext().cacheDir
//
//    private val cache = cachedDir?.let { Cache(it, 10 * 1024 * 1024) }
//
//
//    private val httpClient = OkHttpClient.Builder()
//        .cache(cache)
//        .addInterceptor(RetryInterceptor())
//        .writeTimeout(200, TimeUnit.SECONDS)
//        .readTimeout(200, TimeUnit.SECONDS)
//        .callTimeout(200, TimeUnit.SECONDS)
//        .addInterceptor { chain ->
//            val originalRequest = chain.request()
//            val accessToken = sharedPreferences.getString(TOKEN_KEY, "") ?: ""
//            if (accessToken.isNotEmpty()) {
//                val requestWithAuth = originalRequest.newBuilder()
//                    .header("Authorization", "Bearer $accessToken")
//                    .build()
//                chain.proceed(requestWithAuth)
//            } else {
//                chain.proceed(originalRequest)
//            }
//        }
//        .build()
//
//    val instance: ApiService by lazy {
//        val retrofit = Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .client(httpClient)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//        retrofit.create(ApiService::class.java)
//    }
//}
