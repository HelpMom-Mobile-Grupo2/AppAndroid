package com.helpmom.mother.feature.telemetry.data

import com.helpmom.mother.core.database.MeasurementDao
import com.helpmom.mother.core.database.MeasurementEntity
import com.helpmom.mother.core.network.ApiError
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class TelemetryDto(val id: String, val motherUserId: String, val fetalHeartRateBpm: Int,
                        val temperatureCelsius: Double, val measuredAt: String, val healthLight: Int,
                        val healthSummary: String)
data class TelemetryRequest(val fetalHeartRateBpm: Int, val temperatureCelsius: Double, val measuredAt: String? = null)
interface TelemetryApi {
    @POST("api/telemetry") suspend fun create(@Body request: TelemetryRequest): Response<TelemetryDto>
    @GET("api/telemetry/status") suspend fun status(): Response<TelemetryDto>
    @GET("api/telemetry/history") suspend fun history(@Query("count") count: Int = 20): Response<List<TelemetryDto>>
}
class TelemetryRepository(private val api: TelemetryApi, private val dao: MeasurementDao) {
    val cachedHistory = dao.observeMeasurements()
    suspend fun refreshHistory(): Result<List<TelemetryDto>> = runCatching {
        val response = api.history()
        if (!response.isSuccessful) throw ApiError.from(response)
        response.body().orEmpty().also { rows -> dao.replaceAll(rows.map { it.toEntity() }) }
    }
    suspend fun record(request: TelemetryRequest): Result<TelemetryDto> = runCatching {
        val response = api.create(request)
        if (!response.isSuccessful) throw ApiError.from(response)
        response.body()!!.also { dao.insertAll(listOf(it.toEntity())) }
    }
    private fun TelemetryDto.toEntity() = MeasurementEntity(id, fetalHeartRateBpm, temperatureCelsius, measuredAt, healthLight, healthSummary)
}
