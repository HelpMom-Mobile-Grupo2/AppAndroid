package com.helpmom.mother.feature.auth.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpmom.mother.core.session.SessionStore
import com.helpmom.mother.core.ui.UiState
import com.helpmom.mother.feature.auth.data.AuthRepository
import com.helpmom.mother.feature.auth.data.AuthResponseDto
import com.helpmom.mother.feature.pregnancy.data.PregnancyApi
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository,
    private val pregnancyApi: PregnancyApi? = null,
    private val sessionStore: SessionStore? = null
) : ViewModel() {

    var state: UiState<AuthResponseDto> by mutableStateOf(UiState.Empty)
        private set
    var fieldErrors: Map<String, String> by mutableStateOf(emptyMap())
        private set

    fun login(email: String, password: String) = submit { repository.login(email, password) }
    fun register(name: String, email: String, password: String) = submit { repository.register(name, email, password) }

    private fun submit(request: suspend () -> Result<AuthResponseDto>) {
        viewModelScope.launch {
            state = UiState.Loading
            fieldErrors = emptyMap()
            request().onSuccess { result ->
                if (result.role != "Mother") {
                    state = UiState.Error("Esta cuenta es de acompañante: usa la app HelpMom Compañero")
                } else {
                    repository.saveMotherSession(result)
                    if (pregnancyApi != null && sessionStore != null) {
                        runCatching { pregnancyApi.getPregnancies().body()?.firstOrNull() }
                            .onSuccess { p -> p?.id?.let { sessionStore.savePregnancyId(it) } }
                    }
                    state = UiState.Success(result)
                }
            }.onFailure { e ->
                if (e is com.helpmom.mother.core.network.ApiError && e.errors.isNotEmpty()) {
                    fieldErrors = e.errors.mapKeys { it.key.lowercase() }.mapValues { it.value.firstOrNull().orEmpty() }
                    state = UiState.Error(e.message ?: "Revisa tus datos.")
                } else {
                    state = if (e is java.io.IOException) UiState.Offline else UiState.Error(e.message ?: "No pudimos ingresar.")
                }
            }
        }
    }
}
