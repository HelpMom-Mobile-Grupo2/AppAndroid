package com.helpmom.mother

import android.app.Application
import com.helpmom.mother.core.network.RetrofitClient
import com.helpmom.mother.core.session.SessionStore

class HelpMomApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

class AppContainer(application: Application) {
    val sessionStore = SessionStore(application)
    val database = androidx.room.Room.databaseBuilder(
        application,
        com.helpmom.mother.core.database.HelpMomDatabase::class.java,
        "helpmom_database"
    ).build()

    @Volatile
    private var unauthorizedHandler: (() -> Unit)? = null

    val retrofitClient = RetrofitClient(sessionStore) { unauthorizedHandler?.invoke() }
    val authApi = retrofitClient.createApi(com.helpmom.mother.feature.auth.data.AuthApi::class.java)
    val pregnancyApi = retrofitClient.createApi(com.helpmom.mother.feature.pregnancy.data.PregnancyApi::class.java)
    val telemetryApi = retrofitClient.createApi(com.helpmom.mother.feature.telemetry.data.TelemetryApi::class.java)
    val alertsApi = retrofitClient.createApi(com.helpmom.mother.feature.alerts.data.AlertsApi::class.java)
    val triageApi = retrofitClient.createApi(com.helpmom.mother.feature.triage.data.TriageApi::class.java)
    val familyApi = retrofitClient.createApi(com.helpmom.mother.feature.family.data.FamilyApi::class.java)
    val authRepository = com.helpmom.mother.feature.auth.data.AuthRepository(authApi) { response ->
        sessionStore.saveSession(
            response.accessToken, response.user.id, response.user.fullName, response.user.email,
            response.role, response.familyGroupId
        )
    }
    val telemetryRepository = com.helpmom.mother.feature.telemetry.data.TelemetryRepository(telemetryApi, database.measurementDao())
    val alertsRepository = com.helpmom.mother.feature.alerts.data.AlertsRepository(alertsApi)
    val triageRepository = com.helpmom.mother.feature.triage.data.TriageRepository(triageApi)

    fun setUnauthorizedHandler(handler: (() -> Unit)?) {
        unauthorizedHandler = handler
    }

    suspend fun clearLocalData() {
        database.clearAllTables()
    }
}
