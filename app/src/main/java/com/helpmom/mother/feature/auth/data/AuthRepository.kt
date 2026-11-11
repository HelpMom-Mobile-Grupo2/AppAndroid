package com.helpmom.mother.feature.auth.data

import com.helpmom.mother.core.network.ApiError
import com.helpmom.mother.core.session.SessionStore

class AuthRepository(private val api: AuthApi, private val sessionStore: SessionStore) {
    suspend fun login(email: String, password: String): Result<AuthResponseDto> = runCatching {
        api.login(AuthRequest(email, password)).requireBody()
    }
    suspend fun register(fullName: String, email: String, password: String): Result<AuthResponseDto> = runCatching {
        api.register(RegisterRequest(email, fullName, password)).requireBody()
    }
    suspend fun saveMotherSession(response: AuthResponseDto) = sessionStore.saveSession(
        response.accessToken, response.user.id, response.user.fullName, response.user.email,
        response.role, response.familyGroupId
    )
}
private fun <T> retrofit2.Response<T>.requireBody(): T {
    if (isSuccessful) return body() ?: error("La respuesta del servidor está vacía.")
    throw ApiError.from(this)
}
