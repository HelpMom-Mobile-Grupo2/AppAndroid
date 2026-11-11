package com.helpmom.mother.core.network

import com.helpmom.mother.BuildConfig
import com.helpmom.mother.core.session.SessionStore
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient(
    sessionStore: SessionStore,
    onUnauthorized: () -> Unit
) {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(AuthInterceptor(sessionStore, onUnauthorized))
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun <T : Any> createApi(apiClass: Class<T>): T = retrofit.create(apiClass)
}
