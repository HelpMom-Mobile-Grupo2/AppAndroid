package com.helpmom.mother.feature.alerts.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class QuickAlertDto(val id: String, val type: Int, val message: String?, val createdAt: String,
                         val status: Int, val response: Int?, val respondedAt: String?)
data class CreateAlertRequest(val type: Int, val message: String? = null)
interface AlertsApi {
    @POST("api/alerts") suspend fun createAlert(@Body request: CreateAlertRequest): Response<QuickAlertDto>
    @GET("api/alerts") suspend fun getAlerts(@Query("status") status: String = "all"): Response<List<QuickAlertDto>>
}
