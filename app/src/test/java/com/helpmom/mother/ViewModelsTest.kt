package com.helpmom.mother

import com.helpmom.mother.core.database.MeasurementDao
import com.helpmom.mother.core.database.MeasurementEntity
import com.helpmom.mother.core.ui.UiState
import com.helpmom.mother.feature.auth.data.AuthApi
import com.helpmom.mother.feature.auth.data.AuthRepository
import com.helpmom.mother.feature.auth.data.AuthResponseDto
import com.helpmom.mother.feature.auth.data.UserDto
import com.helpmom.mother.feature.auth.ui.AuthViewModel
import com.helpmom.mother.feature.pregnancy.domain.babyFruitInfo
import com.helpmom.mother.feature.pregnancy.domain.trimester
import com.helpmom.mother.feature.telemetry.data.TelemetryApi
import com.helpmom.mother.feature.telemetry.data.TelemetryDto
import com.helpmom.mother.feature.telemetry.data.TelemetryRepository
import com.helpmom.mother.feature.telemetry.data.TelemetryRequest
import com.helpmom.mother.feature.telemetry.ui.TelemetryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
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

    @Test
    fun trimesterReturnsCorrectTrimesterForWeeks8_20_36() {
        assertEquals(1, trimester(8))
        assertEquals(2, trimester(20))
        assertEquals(3, trimester(36))

        val info8 = babyFruitInfo(8)
        assertEquals("frambuesa", info8.fruitName)

        val info20 = babyFruitInfo(20)
        assertEquals("mango", info20.fruitName)

        val info36 = babyFruitInfo(36)
        assertEquals("melón", info36.fruitName)
    }

    @Test
    fun authViewModelRejectsPartnerRole() = runTest {
        val fakeApi = object : AuthApi {
            override suspend fun login(request: com.helpmom.mother.feature.auth.data.LoginRequest): Response<AuthResponseDto> {
                return Response.success(
                    AuthResponseDto(
                        user = UserDto("1", "partner@test.com", "Carlos Partner", true),
                        familyGroupId = "group-1",
                        role = "Partner",
                        accessToken = "token-123"
                    )
                )
            }

            override suspend fun register(request: com.helpmom.mother.feature.auth.data.RegisterRequest): Response<AuthResponseDto> {
                throw UnsupportedOperationException()
            }
        }

        val repo = object : AuthRepository(fakeApi, DummySessionStore()) {
            override suspend fun login(email: String, password: String): Result<AuthResponseDto> {
                return Result.success(
                    AuthResponseDto(
                        user = UserDto("1", "partner@test.com", "Carlos Partner", true),
                        familyGroupId = "group-1",
                        role = "Partner",
                        accessToken = "token-123"
                    )
                )
            }
            override suspend fun saveMotherSession(dto: AuthResponseDto) {}
        }

        val vm = AuthViewModel(repo)

        vm.login("partner@test.com", "password12345")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state is UiState.Error)
        val errorState = vm.state as UiState.Error
        assertTrue(errorState.message.contains("cuenta es de acompañante"))
    }

    @Test
    fun telemetryRecordUpdatesStateToSuccess() = runTest {
        val fakeApi = object : TelemetryApi {
            override suspend fun record(request: TelemetryRequest): Response<TelemetryDto> {
                return Response.success(
                    TelemetryDto(
                        id = "m1",
                        motherUserId = "mom1",
                        fetalHeartRateBpm = request.fetalHeartRateBpm,
                        temperatureCelsius = request.temperatureCelsius,
                        measuredAt = "2026-11-03T10:00:00+00:00",
                        healthLight = 1,
                        healthSummary = "Valores normales."
                    )
                )
            }

            override suspend fun status(): Response<TelemetryDto> {
                return Response.success(
                    TelemetryDto("m1", "mom1", 140, 36.8, "2026-11-03T10:00:00+00:00", 1, "Valores normales.")
                )
            }

            override suspend fun history(count: Int): Response<List<TelemetryDto>> {
                return Response.success(
                    listOf(TelemetryDto("m1", "mom1", 140, 36.8, "2026-11-03T10:00:00+00:00", 1, "Valores normales."))
                )
            }
        }

        val fakeDao = FakeMeasurementDao()
        val repo = TelemetryRepository(fakeApi, fakeDao)
        val vm = TelemetryViewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.record(145, 36.7)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state is UiState.Success)
        val successState = vm.state as UiState.Success
        assertEquals(145, successState.data.fetalHeartRateBpm)
        assertEquals(36.7, successState.data.temperatureCelsius, 0.01)
    }
}

private class DummySessionStore : com.helpmom.mother.core.session.SessionStore(
    // Null context not called since methods overridden or unused
    DummyContext()
)

private class DummyContext : android.content.ContextWrapper(null)

private class FakeMeasurementDao : MeasurementDao {
    private val items = mutableListOf<MeasurementEntity>()
    override fun observeLatest(): Flow<MeasurementEntity?> = flowOf(items.firstOrNull())
    override fun observeHistory(): Flow<List<MeasurementEntity>> = flowOf(items)
    override suspend fun insertAll(measurements: List<MeasurementEntity>) {
        items.clear()
        items.addAll(measurements)
    }
    override suspend fun clearAll() { items.clear() }
}
