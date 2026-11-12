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
    /** true cuando el backend responde 409: la mamá aún no tiene acompañante vinculado. */
    var needsPartner: Boolean by mutableStateOf(false)
        private set
    var sent: Boolean by mutableStateOf(false)
        private set

    fun loadPartnerName() {
        viewModelScope.launch {
            runCatching { familyApi.getFamily().body() }
                .onSuccess { family ->
                    partnerName = family?.members?.firstOrNull { it.role == 3 }?.fullName
                }
        }
    }

    fun sendAlert(type: Int, message: String = "") {
        if (isLoading) return
        viewModelScope.launch {
            isLoading = true
            resultMessage = ""
            needsPartner = false
            val displayName = partnerName ?: "Tu acompañante"
            val text = message.trim().take(500).ifBlank { null }
            val result = runCatching {
                alertsApi.createAlert(CreateAlertRequest(type = type, message = text))
            }

            isLoading = false
            val response = result.getOrNull()
            when {
                response?.isSuccessful == true -> {
                    sent = true
                    resultMessage = "$displayName fue notificado."
                }
                response?.code() == 409 -> {
                    needsPartner = true
                    resultMessage = "Aún no vinculas a tu acompañante."
                }
                result.exceptionOrNull() is java.io.IOException ->
                    resultMessage = "Sin conexión. Inténtalo nuevamente."
                else -> resultMessage = "No se pudo enviar. Inténtalo nuevamente."
            }
        }
    }
}
