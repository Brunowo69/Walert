package com.example.walert.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.walert.datastore.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.clearInvocations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class WaterViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    private lateinit var mockUserPreferences: UserPreferences

    @Mock
    private lateinit var mockWorkManager: WorkManager

    @Mock
    private lateinit var mockTimerProvider: TimerProvider

    private lateinit var waterViewModel: WaterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        whenever(mockUserPreferences.waterCountFlow).thenReturn(flowOf(0))
        whenever(mockUserPreferences.lastCountDateFlow).thenReturn(flowOf(LocalDate.now().toString()))
        whenever(mockUserPreferences.waterHistoryFlow).thenReturn(flowOf(emptyList()))
        whenever(mockUserPreferences.unlockedAchievementsFlow).thenReturn(flowOf(emptySet()))
        whenever(mockUserPreferences.notificationHistoryFlow).thenReturn(flowOf(emptySet()))
        whenever(mockUserPreferences.reminderIntervalFlow).thenReturn(flowOf(2))
        whenever(mockUserPreferences.userPointsFlow).thenReturn(flowOf(0))
        whenever(mockUserPreferences.purchasedItemsFlow).thenReturn(flowOf(emptySet()))
        whenever(mockUserPreferences.selectedThemeFlow).thenReturn(flowOf("default"))
        whenever(mockUserPreferences.dailyGoalFlow).thenReturn(flowOf(8))

        val mockTimer: CountDownTimerWrapper = mock()
        whenever(mockTimerProvider.create(any(), any(), any(), any())).thenReturn(mockTimer)

        waterViewModel = WaterViewModel(
            userPreferences = mockUserPreferences,
            workManager = mockWorkManager,
            timerProvider = mockTimerProvider
        )

        // Clear invocations that happened during setup (in ViewModel's init block)
        clearInvocations(mockUserPreferences, mockWorkManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cuando se inicia el viewmodel, el contador de agua es cero`() = runTest {
        assertEquals(0, waterViewModel.currentWaterCount.value)
    }

    @Test
    fun `al añadir una bebida, el contador de agua y los puntos se actualizan`() = runTest {
        val beverage = Beverage.WATER
        val glasses = 2

        waterViewModel.addDrink(beverage, glasses)

        assertEquals(glasses, waterViewModel.currentWaterCount.value)
        assertEquals(beverage.points * glasses, waterViewModel.userPoints.value)

        verify(mockUserPreferences).saveWaterCount(glasses)
        verify(mockUserPreferences).saveUserPoints(beverage.points * glasses)
        verify(mockWorkManager).enqueue(any<OneTimeWorkRequest>())
    }

    @Test
    fun `al resetear el contador, se borran todos los datos del usuario`() = runTest {
        waterViewModel.resetWaterCount()

        verify(mockUserPreferences).clearAllData()
    }

    @Test
    fun `al quitar agua, el contador se actualiza correctamente`() = runTest {
        waterViewModel.addDrink(Beverage.WATER, 2)
        assertEquals(2, waterViewModel.currentWaterCount.value)

        // Clear invocations from the arrange step above
        clearInvocations(mockUserPreferences)

        val glassesToRemove = 1
        waterViewModel.removeWater(glassesToRemove)

        assertEquals(1, waterViewModel.currentWaterCount.value)
        verify(mockUserPreferences).saveWaterCount(1)
    }
}
