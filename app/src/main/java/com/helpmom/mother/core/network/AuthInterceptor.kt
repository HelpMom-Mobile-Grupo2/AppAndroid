package com.helpmom.mother.core.network

import com.helpmom.mother.core.session.SessionStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val sessionStore: SessionStore,
    private val onUnauthorized: () -> Unit
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { sessionStore.session.first().token }
        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrBlank()) {
                header("Authorization", "Bearer $token")
            }
        }.build()
        val response = chain.proceed(request)

        if (response.code == 401) {
            runBlocking { sessionStore.clear() }
            onUnauthorized()
        }

        return response
    }
}
