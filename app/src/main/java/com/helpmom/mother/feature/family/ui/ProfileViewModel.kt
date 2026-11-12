package com.helpmom.mother.feature.family.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpmom.mother.core.network.ApiError
import com.helpmom.mother.core.session.SessionStore
import com.helpmom.mother.feature.family.data.FamilyApi
import com.helpmom.mother.feature.pregnancy.data.PregnancyApi
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val familyApi: FamilyApi,
    private val pregnancyApi: PregnancyApi,
    private val sessionStore: SessionStore
) : ViewModel() {

    var name: String by mutableStateOf("")
        private set
    var email: String by mutableStateOf("")
        private set
    /** Nombre del acompañante, o null si no hay uno vinculado. */
    var partner: String? by mutableStateOf(null)
        private set
    var isLoading: Boolean by mutableStateOf(true)
        private set
    var weekText: String by mutableStateOf("")
        private set
    var errorMessage: String? by mutableStateOf(null)
        private set
    var isUnlinking: Boolean by mutableStateOf(false)
        private set

    fun loadProfile() {
        viewModelScope.launch {
            isLoading = true
            val session = sessionStore.current()
            name = session.fullName.orEmpty()
            email = session.email.orEmpty()
            runCatching { familyApi.getFamily().body()?.members?.firstOrNull { it.role == 3 }?.fullName }
                .onSuccess { partner = it }
                .onFailure { errorMessage = "No pudimos cargar a tu acompañante." }
            runCatching { pregnancyApi.getPregnancies().body()?.firstOrNull() }
                .onSuccess { p ->
                    weekText = if (p != null) "Semana ${p.gestationalWeeks} · Día ${p.gestationalDays}" else ""
                }
            isLoading = false
        }
    }

    fun unlinkPartner() {
        if (isUnlinking) return
        viewModelScope.launch {
            isUnlinking = true
            errorMessage = null
            runCatching { familyApi.unlinkPartner() }
                .onSuccess { response ->
                    if (response.isSuccessful) partner = null
                    else errorMessage = ApiError.from(response).detail
                }
                .onFailure {
                    errorMessage = if (it is java.io.IOException) "Sin conexión. No se pudo desvincular."
                    else "No se pudo desvincular. Inténtalo de nuevo."
                }
            isUnlinking = false
        }
    }
}
