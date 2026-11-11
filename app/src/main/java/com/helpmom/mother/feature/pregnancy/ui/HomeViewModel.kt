package com.helpmom.mother.feature.pregnancy.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpmom.mother.core.session.SessionStore
import com.helpmom.mother.feature.alerts.data.AlertsApi
import com.helpmom.mother.feature.alerts.data.QuickAlertDto
import com.helpmom.mother.feature.family.data.FamilyApi
import com.helpmom.mother.feature.pregnancy.data.PregnancyApi
import com.helpmom.mother.feature.pregnancy.data.PregnancyDto
import com.helpmom.mother.feature.telemetry.data.TelemetryApi
import com.helpmom.mother.feature.telemetry.data.TelemetryDto
import kotlinx.coroutines.launch

class HomeViewModel(
    private val pregnancyApi: PregnancyApi,
    private val familyApi: FamilyApi,
    private val telemetryApi: TelemetryApi,
    private val alertsApi: AlertsApi,
    private val sessionStore: SessionStore
) : ViewModel() {

    var pregnancy: PregnancyDto? by mutableStateOf(null)
        private set
    var partnerName: String? by mutableStateOf(null)
        private set
    var motherInitials: String by mutableStateOf("M")
        private set
    var latestAlert: QuickAlertDto? by mutableStateOf(null)
        private set
    var healthStatus: TelemetryDto? by mutableStateOf(null)
        private set
    var isLoading: Boolean by mutableStateOf(false)
        private set

    fun loadData() {
        viewModelScope.launch {
            isLoading = true
            val session = sessionStore.current()
            session.fullName?.takeIf { it.isNotBlank() }?.let { name ->
                motherInitials = name.split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .map { it.first().uppercaseChar() }
                    .joinToString("")
                    .ifBlank { "M" }
            }

            runCatching { pregnancyApi.getPregnancies().body()?.firstOrNull() }
                .onSuccess { pregnancy = it }

            runCatching { familyApi.getFamily().body() }
                .onSuccess { family ->
                    partnerName = family?.members?.firstOrNull { it.role == 3 }?.fullName
                }

            runCatching { telemetryApi.status().body() }
                .onSuccess { healthStatus = it }

            runCatching { alertsApi.getAlerts("all").body()?.firstOrNull() }
                .onSuccess { latestAlert = it }

            isLoading = false
        }
    }

    fun pollAlerts() {
        viewModelScope.launch {
            runCatching { alertsApi.getAlerts("all").body()?.firstOrNull() }
                .onSuccess { latestAlert = it }
        }
    }
}
