package com.helpmom.mother.feature.triage.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpmom.mother.core.network.ApiError
import com.helpmom.mother.feature.triage.data.TriageApi
import com.helpmom.mother.feature.triage.data.TriageConversationDto
import com.helpmom.mother.feature.triage.data.TriageMessageRequest
import com.helpmom.mother.feature.triage.data.TriageSummaryDto
import kotlinx.coroutines.launch

class ChatViewModel(private val triageApi: TriageApi) : ViewModel() {

    var conversation: TriageConversationDto? by mutableStateOf(null)
        private set
    var inputMessage: String by mutableStateOf("")
    var errorMessage: String by mutableStateOf("")
        private set
    var emergencyGuidance: String? by mutableStateOf(null)
    var isSending: Boolean by mutableStateOf(false)
        private set
    var history: List<TriageSummaryDto> by mutableStateOf(emptyList())
        private set
    var isLoadingHistory: Boolean by mutableStateOf(false)
        private set

    /** GET /api/triage: conversaciones anteriores, la más reciente primero. */
    fun loadHistory() {
        viewModelScope.launch {
            isLoadingHistory = true
            runCatching { triageApi.conversations() }
                .onSuccess { response ->
                    if (response.isSuccessful) history = response.body().orEmpty().sortedByDescending { it.createdAt }
                    else errorMessage = ApiError.from(response).detail
                }
                .onFailure { errorMessage = "Sin conexión. No se pudo cargar el historial." }
            isLoadingHistory = false
        }
    }

    /** GET /api/triage/{id}: abre una conversación anterior para seguirla. */
    fun openConversation(id: String) {
        viewModelScope.launch {
            errorMessage = ""
            runCatching { triageApi.conversation(id) }
                .onSuccess { response ->
                    if (response.isSuccessful) conversation = response.body()
                    else errorMessage = ApiError.from(response).detail
                }
                .onFailure { errorMessage = "Sin conexión. No se pudo abrir la conversación." }
        }
    }

    fun newConversation() {
        conversation = null
        errorMessage = ""
    }

    fun sendMessage() {
        val content = inputMessage.trim()
        if (content.isEmpty() || isSending) return

        viewModelScope.launch {
            isSending = true
            inputMessage = ""
            errorMessage = ""

            val result = runCatching {
                val currentConv = conversation
                val response = if (currentConv == null) {
                    triageApi.start(TriageMessageRequest(content))
                } else {
                    triageApi.send(currentConv.id, TriageMessageRequest(content))
                }
                if (!response.isSuccessful) throw ApiError.from(response)
                response.body()!!
            }

            isSending = false
            result.onSuccess { res ->
                conversation = res
                if (res.urgency == 3) {
                    emergencyGuidance = res.guidance ?: "Busca ayuda médica de inmediato."
                }
            }.onFailure { err ->
                inputMessage = content
                errorMessage = if (err is java.io.IOException) "Sin conexión. Tu mensaje no se envió."
                else err.message ?: "No pudimos enviar tu mensaje."
            }
        }
    }

    fun dismissEmergency() {
        emergencyGuidance = null
    }
}
