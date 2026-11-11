package com.helpmom.mother.core.session

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "helpmom_session")

data class SessionData(
    val token: String? = null,
    val userId: String? = null,
    val fullName: String? = null,
    val email: String? = null,
    val role: String? = null,
    val familyGroupId: String? = null,
    val pregnancyId: String? = null,
    val termsAcceptedAt: Long? = null
)

class SessionStore(context: Context) {
    private val dataStore = context.applicationContext.sessionDataStore

    val session: Flow<SessionData> = dataStore.data.map { preferences ->
        SessionData(
            token = preferences[TOKEN],
            userId = preferences[USER_ID],
            fullName = preferences[FULL_NAME],
            email = preferences[EMAIL],
            role = preferences[ROLE],
            familyGroupId = preferences[FAMILY_GROUP_ID],
            pregnancyId = preferences[PREGNANCY_ID],
            termsAcceptedAt = preferences[TERMS_ACCEPTED_AT]
        )
    }

    suspend fun current(): SessionData = session.first()

    suspend fun saveSession(
        token: String,
        userId: String,
        fullName: String,
        email: String,
        role: String,
        familyGroupId: String?
    ) {
        dataStore.edit { preferences ->
            preferences[TOKEN] = token
            preferences[USER_ID] = userId
            preferences[FULL_NAME] = fullName
            preferences[EMAIL] = email
            preferences[ROLE] = role
            if (familyGroupId == null) {
                preferences.remove(FAMILY_GROUP_ID)
            } else {
                preferences[FAMILY_GROUP_ID] = familyGroupId
            }
        }
    }

    suspend fun savePregnancyId(pregnancyId: String) {
        dataStore.edit { it[PREGNANCY_ID] = pregnancyId }
    }

    suspend fun acceptTerms(acceptedAtEpochMillis: Long) {
        dataStore.edit {
            it[TERMS_ACCEPTED] = true
            it[TERMS_ACCEPTED_AT] = acceptedAtEpochMillis
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private companion object {
        val TOKEN = stringPreferencesKey("access_token")
        val USER_ID = stringPreferencesKey("user_id")
        val FULL_NAME = stringPreferencesKey("full_name")
        val EMAIL = stringPreferencesKey("email")
        val ROLE = stringPreferencesKey("role")
        val FAMILY_GROUP_ID = stringPreferencesKey("family_group_id")
        val PREGNANCY_ID = stringPreferencesKey("pregnancy_id")
        val TERMS_ACCEPTED = booleanPreferencesKey("terms_accepted")
        val TERMS_ACCEPTED_AT = longPreferencesKey("terms_accepted_at")
    }
}
