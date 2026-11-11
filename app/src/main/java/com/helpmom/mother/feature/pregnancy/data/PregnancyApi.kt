package com.helpmom.mother.feature.pregnancy.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class SymptomDto(val id: String, val description: String, val severity: Int, val occurredAt: String)
data class PregnancyDto(
    val id: String, val motherUserId: String, val lastMenstrualPeriod: String, val dueDate: String,
    val gestationalWeeks: Int, val gestationalDays: Int, val symptoms: List<SymptomDto> = emptyList()
)
data class PregnancyRequest(val lastMenstrualPeriod: String, val dueDate: String)
data class SymptomRequest(val description: String, val severity: Int, val occurredAt: String? = null)

interface PregnancyApi {
    @GET("api/pregnancy") suspend fun getPregnancies(): Response<List<PregnancyDto>>
    @GET("api/pregnancy/{id}") suspend fun getPregnancy(@Path("id") id: String): Response<PregnancyDto>
    @POST("api/pregnancy") suspend fun createPregnancy(@Body request: PregnancyRequest): Response<PregnancyDto>
    @POST("api/pregnancy/{pregnancyId}/symptoms") suspend fun addSymptom(@Path("pregnancyId") pregnancyId: String, @Body request: SymptomRequest): Response<SymptomDto>
}
