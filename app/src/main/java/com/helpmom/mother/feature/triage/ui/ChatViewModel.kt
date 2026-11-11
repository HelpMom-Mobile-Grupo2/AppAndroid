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
                errorMessage = err.message ?: "No pudimos enviar tu mensaje."
            }
        }
    }

    fun dismissEmergency() {
        emergencyGuidance = null
    }
}
