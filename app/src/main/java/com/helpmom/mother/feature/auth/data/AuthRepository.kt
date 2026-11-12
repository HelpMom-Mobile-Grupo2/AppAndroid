package com.helpmom.mother.feature.auth.data

import com.helpmom.mother.core.network.ApiError

/**
 * Login y registro contra /api/auth.
 * saveSession guarda el token en DataStore (en los tests se reemplaza por una función falsa).
 */
class AuthRepository(
    private val api: AuthApi,
    private val saveSession: suspend (AuthResponseDto) -> Unit
) {
    suspend fun login(email: String, password: String): Result<AuthResponseDto> = runCatching {
        api.login(AuthRequest(email, password)).requireBody()
    }
    suspend fun register(fullName: String, email: String, password: String): Result<AuthResponseDto> = runCatching {
        api.register(RegisterRequest(email, fullName, password)).requireBody()
    }
    suspend fun saveMotherSession(response: AuthResponseDto) = saveSession(response)
}
private fun <T> retrofit2.Response<T>.requireBody(): T {
    if (isSuccessful) return body() ?: error("La respuesta del servidor está vacía.")
    throw ApiError.from(this)
}
