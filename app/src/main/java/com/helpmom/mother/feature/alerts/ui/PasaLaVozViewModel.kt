package com.helpmom.mother.feature.alerts.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpmom.mother.feature.alerts.data.AlertsApi
import com.helpmom.mother.feature.alerts.data.CreateAlertRequest
import com.helpmom.mother.feature.family.data.FamilyApi
import kotlinx.coroutines.launch

class PasaLaVozViewModel(
    private val alertsApi: AlertsApi,
    private val familyApi: FamilyApi
) : ViewModel() {

    var partnerName: String? by mutableStateOf(null)
        private set
    var resultMessage: String by mutableStateOf("")
        private set
    var isLoading: Boolean by mutableStateOf(false)
        private set

    fun loadPartnerName() {
        viewModelScope.launch {
            runCatching { familyApi.getFamily().body() }
                .onSuccess { family ->
                    partnerName = family?.members?.firstOrNull { it.role == 3 }?.fullName
                }
        }
    }

    fun sendAlert(type: Int) {
        viewModelScope.launch {
            isLoading = true
            resultMessage = ""
            val displayName = partnerName ?: "tu acompañante"
            val response = runCatching {
                alertsApi.createAlert(CreateAlertRequest(type = type))
            }.getOrNull()

            isLoading = false
            if (response?.isSuccessful == true) {
                resultMessage = "$displayName fue notificado."
            } else if (response?.code() == 409) {
                resultMessage = "Aún no vinculas a tu acompañante."
            } else {
                resultMessage = "No se pudo enviar. Inténtalo nuevamente."
            }
        }
    }
}
