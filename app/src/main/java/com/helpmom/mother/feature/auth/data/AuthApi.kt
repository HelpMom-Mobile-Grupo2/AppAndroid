package com.helpmom.mother.feature.auth.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class AuthRequest(val email: String, val password: String)
data class RegisterRequest(val email: String, val fullName: String, val password: String)
data class AuthUserDto(val id: String, val email: String, val fullName: String, val isActive: Boolean)
data class AuthResponseDto(val user: AuthUserDto, val familyGroupId: String?, val role: String, val accessToken: String)

interface AuthApi {
    @POST("api/auth/login") suspend fun login(@Body request: AuthRequest): Response<AuthResponseDto>
    @POST("api/auth/register") suspend fun register(@Body request: RegisterRequest): Response<AuthResponseDto>
}
