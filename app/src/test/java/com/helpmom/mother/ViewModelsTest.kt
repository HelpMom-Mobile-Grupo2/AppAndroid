package com.helpmom.mother

import com.helpmom.mother.core.database.MeasurementDao
import com.helpmom.mother.core.database.MeasurementEntity
import com.helpmom.mother.core.ui.UiState
import com.helpmom.mother.feature.alerts.data.AlertsApi
import com.helpmom.mother.feature.alerts.data.CreateAlertRequest
import com.helpmom.mother.feature.alerts.data.QuickAlertDto
import com.helpmom.mother.feature.alerts.ui.PasaLaVozViewModel
import com.helpmom.mother.feature.auth.data.AuthApi
import com.helpmom.mother.feature.auth.data.AuthRepository
import com.helpmom.mother.feature.auth.data.AuthRequest
import com.helpmom.mother.feature.auth.data.AuthResponseDto
import com.helpmom.mother.feature.auth.data.AuthUserDto
import com.helpmom.mother.feature.auth.data.RegisterRequest
import com.helpmom.mother.feature.auth.ui.AuthViewModel
import com.helpmom.mother.feature.family.data.FamilyApi
import com.helpmom.mother.feature.family.data.FamilyGroupDto
import com.helpmom.mother.feature.family.data.FamilyMemberDto
import com.helpmom.mother.feature.family.data.InvitationDto
import com.helpmom.mother.feature.pregnancy.domain.findPregnancyId
import com.helpmom.mother.feature.telemetry.data.TelemetryApi
import com.helpmom.mother.feature.telemetry.data.TelemetryDto
import com.helpmom.mother.feature.telemetry.data.TelemetryRepository
import com.helpmom.mother.feature.telemetry.data.TelemetryRequest
import com.helpmom.mother.feature.telemetry.ui.TelemetryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelsTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---------- Auth ----------

    private fun authResponse(role: String) = AuthResponseDto(
        user = AuthUserDto("1", "correo@test.com", "Ana Pérez", true),
        familyGroupId = "group-1",
        role = role,
        accessToken = "token-123"
    )

    private class FakeAuthApi(private val response: AuthResponseDto) : AuthApi {
        override suspend fun login(request: AuthRequest): Response<AuthResponseDto> = Response.success(response)
        override suspend fun register(request: RegisterRequest): Response<AuthResponseDto> = Response.success(response)
    }

    @Test
    fun authViewModelRejectsPartnerRole() = runTest(testDispatcher) {
        var saved = false
        val repo = AuthRepository(FakeAuthApi(authResponse("Partner"))) { saved = true }
        val vm = AuthViewModel(repo)

        vm.login("pareja@test.com", "password12345")
        advanceUntilIdle()

        val state = vm.state
        assertTrue(state is UiState.Error)
        assertTrue((state as UiState.Error).message.contains("cuenta es de acompañante"))
        assertFalse("No debe guardar la sesión de un acompañante", saved)
    }

    @Test
    fun authViewModelAcceptsMotherAndSavesSession() = runTest(testDispatcher) {
        var saved = false
        val repo = AuthRepository(FakeAuthApi(authResponse("Mother"))) { saved = true }
        val vm = AuthViewModel(repo)

        vm.login("mama@test.com", "password12345")
        advanceUntilIdle()

        assertTrue(vm.state is UiState.Success)
        assertTrue(saved)
    }

    // ---------- Embarazo: Inicio o Configurar embarazo ----------

    @Test
    fun goesHomeWhenBackendAlreadyHasAPregnancy() = runTest {
        val id = findPregnancyId(savedId = null) { "preg-1" }
        assertEquals("preg-1", id)
    }

    @Test
    fun usesSavedPregnancyWithoutCallingTheBackend() = runTest {
        var called = false
        val id = findPregnancyId(savedId = "preg-guardado") { called = true; "otro" }
        assertEquals("preg-guardado", id)
        assertFalse(called)
    }

    @Test
    fun goesToSetupWhenThereIsNoPregnancyOrNoConnection() = runTest {
        assertNull(findPregnancyId(savedId = null) { null })
        assertNull(findPregnancyId(savedId = "") { throw java.io.IOException("sin internet") })
    }

    // ---------- Telemetría ----------

    private class FakeTelemetryApi(private val healthLight: Int) : TelemetryApi {
        override suspend fun create(request: TelemetryRequest): Response<TelemetryDto> = Response.success(
            TelemetryDto(
                id = "m-${request.fetalHeartRateBpm}",
                motherUserId = "mom1",
                fetalHeartRateBpm = request.fetalHeartRateBpm,
                temperatureCelsius = request.temperatureCelsius,
                measuredAt = "2026-11-03T10:00:00+00:00",
                healthLight = healthLight,
                healthSummary = "Resumen"
            )
        )
        override suspend fun status(): Response<TelemetryDto> = Response.error(404, "".toResponseBody(null))
        override suspend fun history(count: Int): Response<List<TelemetryDto>> = Response.success(emptyList())
    }

    private class FakeMeasurementDao : MeasurementDao {
        private val items = MutableStateFlow<List<MeasurementEntity>>(emptyList())
        override fun observeMeasurements(): Flow<List<MeasurementEntity>> = items
        override suspend fun insertAll(measurements: List<MeasurementEntity>) {
            items.value = measurements + items.value
        }
        override suspend fun clear() { items.value = emptyList() }
        override suspend fun deleteAll() { items.value = emptyList() }
    }

    @Test
    fun telemetryRecordUpdatesState() = runTest(testDispatcher) {
        val vm = TelemetryViewModel(TelemetryRepository(FakeTelemetryApi(healthLight = 1), FakeMeasurementDao()))
        advanceUntilIdle()

        vm.record(145, 36.7)
        advanceUntilIdle()

        val state = vm.state
        assertTrue(state is UiState.Success)
        assertEquals(145, (state as UiState.Success).data.fetalHeartRateBpm)
        assertNull("Una medición verde no abre el modal", vm.pendingEmergency)
    }

    @Test
    fun redMeasurementOpensEmergencyEveryTime() = runTest(testDispatcher) {
        val vm = TelemetryViewModel(TelemetryRepository(FakeTelemetryApi(healthLight = 3), FakeMeasurementDao()))
        advanceUntilIdle()

        vm.record(200, 39.5)
        advanceUntilIdle()
        assertEquals("m-200", vm.pendingEmergency?.id)

        vm.dismissEmergency()
        vm.record(210, 39.8)
        advanceUntilIdle()
        assertEquals("m-210", vm.pendingEmergency?.id)
    }

    // ---------- Pasa la Voz ----------

    private class FakeAlertsApi(private val code: Int) : AlertsApi {
        override suspend fun createAlert(request: CreateAlertRequest): Response<QuickAlertDto> =
            if (code == 201) Response.success(QuickAlertDto("a1", request.type, request.message, "2026-11-03T10:00:00+00:00", 1, null, null))
            else Response.error(code, "".toResponseBody(null))
        override suspend fun getAlerts(status: String): Response<List<QuickAlertDto>> = Response.success(emptyList())
    }

    private class FakeFamilyApi(private val partnerName: String?) : FamilyApi {
        override suspend fun createInvitation(): Response<InvitationDto> = Response.success(InvitationDto("HM-1234", "2026-11-04T10:00:00+00:00"))
        override suspend fun getFamily(): Response<FamilyGroupDto> = Response.success(
            FamilyGroupDto(
                "g1", "Familia",
                listOfNotNull(
                    FamilyMemberDto("u1", "Ana", 2, "2026-11-01T10:00:00+00:00"),
                    partnerName?.let { FamilyMemberDto("u2", it, 3, "2026-11-01T10:00:00+00:00") }
                )
            )
        )
        override suspend fun unlinkPartner(): Response<Unit> = Response.success(Unit)
    }

    @Test
    fun pasaLaVozSaysWhoWasNotified() = runTest(testDispatcher) {
        val vm = PasaLaVozViewModel(FakeAlertsApi(201), FakeFamilyApi("Mateo"))
        vm.loadPartnerName()
        advanceUntilIdle()

        vm.sendAlert(1, "Quiero mango")
        advanceUntilIdle()

        assertEquals("Mateo fue notificado.", vm.resultMessage)
        assertTrue(vm.sent)
    }

    @Test
    fun pasaLaVozOffersLinkingOn409() = runTest(testDispatcher) {
        val vm = PasaLaVozViewModel(FakeAlertsApi(409), FakeFamilyApi(null))
        vm.sendAlert(2)
        advanceUntilIdle()

        assertTrue(vm.needsPartner)
        assertFalse(vm.sent)
    }
}
