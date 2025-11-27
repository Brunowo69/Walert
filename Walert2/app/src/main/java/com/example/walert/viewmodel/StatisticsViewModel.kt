package com.example.walert.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.walert.datastore.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class WeeklyData(val day: String, val amount: Int)
data class StatisticsState(
    val weeklyConsumption: List<WeeklyData> = emptyList(),
    val streak: Int = 0,
    val dailyAverage: Float = 0f,
    val bestDay: Int = 0
)

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)
    private val _uiState = MutableStateFlow(StatisticsState())
    val uiState: StateFlow<StatisticsState> = _uiState.asStateFlow()

    init {
        combine(userPreferences.waterHistoryFlow, userPreferences.dailyGoalFlow) { history, goal ->
            val weeklyConsumption = calculateWeeklyConsumption(history)
            val streak = calculateStreak(history, goal)
            val dailyAverage = calculateDailyAverage(history)
            val bestDay = history.maxOfOrNull { it.amount } ?: 0

            StatisticsState(
                weeklyConsumption = weeklyConsumption,
                streak = streak,
                dailyAverage = dailyAverage,
                bestDay = bestDay
            )
        }.onEach { newState ->
            _uiState.value = newState
        }.launchIn(viewModelScope)
    }

    private fun calculateWeeklyConsumption(history: List<WaterRecord>): List<WeeklyData> {
        val today = LocalDate.now()
        val weekStart = today.with(DayOfWeek.MONDAY)
        val weekData = mutableListOf<WeeklyData>()

        for (i in 0..6) {
            val date = weekStart.plusDays(i.toLong())
            val record = history.find { it.date == date }
            val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            weekData.add(WeeklyData(dayName, record?.amount ?: 0))
        }
        return weekData
    }

    private fun calculateDailyAverage(history: List<WaterRecord>): Float {
        if (history.isEmpty()) return 0f
        val totalAmount = history.sumOf { it.amount }
        return if (history.isNotEmpty()) totalAmount.toFloat() / history.size.toFloat() else 0f
    }

    private fun calculateStreak(history: List<WaterRecord>, goal: Int): Int {
        if (goal <= 0) return 0

        val successfulDays = history.filter { it.amount >= goal }.map { it.date }.toSet()
        if (successfulDays.isEmpty()) return 0

        var streak = 0
        var checkDate = LocalDate.now()

        if (!successfulDays.contains(checkDate)) {
            checkDate = checkDate.minusDays(1)
        }

        while (successfulDays.contains(checkDate)) {
            streak++
            checkDate = checkDate.minusDays(1)
        }

        return streak
    }
}
