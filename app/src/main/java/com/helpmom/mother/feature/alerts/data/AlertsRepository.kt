package com.helpmom.mother.feature.alerts.data

import com.helpmom.mother.core.network.ApiError

class AlertsRepository(private val api: AlertsApi) {
    suspend fun send(type: Int, message: String?): Result<QuickAlertDto> = runCatching {
        api.createAlert(CreateAlertRequest(type, message)).let { if (it.isSuccessful) it.body()!! else throw ApiError.from(it) }
    }
    suspend fun current(): Result<List<QuickAlertDto>> = runCatching {
        api.getAlerts("all").let { if (it.isSuccessful) it.body().orEmpty() else throw ApiError.from(it) }
    }
}
