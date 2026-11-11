package com.helpmom.mother.feature.family.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helpmom.mother.core.session.SessionStore
import com.helpmom.mother.feature.family.data.FamilyApi
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val familyApi: FamilyApi,
    private val sessionStore: SessionStore
) : ViewModel() {

    var name: String by mutableStateOf("")
        private set
    var email: String by mutableStateOf("")
        private set
    var partner: String by mutableStateOf("Cargando…")
        private set

    fun loadProfile() {
        viewModelScope.launch {
            val session = sessionStore.current()
            name = session.fullName.orEmpty().ifBlank { "Daniela Gómez" }
            email = session.email.orEmpty()
            runCatching { familyApi.getFamily().body()?.members?.firstOrNull { it.role == 3 }?.fullName }
                .onSuccess { partner = it ?: "Sin acompañante" }
                .onFailure { partner = "Sin acompañante" }
        }
    }

    fun unlinkPartner() {
        viewModelScope.launch {
            runCatching { familyApi.unlinkPartner() }
            partner = "Sin acompañante"
        }
    }
}
