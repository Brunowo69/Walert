package com.example.walert.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.walert.datastore.UserPreferences
import com.example.walert.model.Achievement
import com.example.walert.workers.ReminderWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

data class WaterRecord(val date: LocalDate, val amount: Int)

enum class Beverage(val displayName: String, val hydrationFactor: Double, val points: Int) {
    WATER("Agua", 1.0, 10),
    TEA("Té", 0.8, 8),
    COFFEE("Café", -0.1, -2),
    JUICE("Zumo", 0.9, 5),
    SODA("Refresco", -0.2, -5),
    ENERGY_DRINK("Bebida Energética", -0.3, -8)
}

data class StoreItem(
    val id: String,
    val name: String,
    val description: String,
    val cost: Int,
    val isPurchased: Boolean = false
)

class WaterViewModel(
    private val userPreferences: UserPreferences,
    private val workManager: WorkManager,
    private val timerProvider: TimerProvider
) : ViewModel() {

    private val _currentWaterCount = MutableStateFlow(0)
    val currentWaterCount: StateFlow<Int> = _currentWaterCount.asStateFlow()

    private val _userPoints = MutableStateFlow(0)
    val userPoints: StateFlow<Int> = _userPoints.asStateFlow()

    private val _storeItems = MutableStateFlow(initialStoreItems)
    val storeItems: StateFlow<List<StoreItem>> = _storeItems.asStateFlow()

    private val _activeThemeName = MutableStateFlow("default")
    val activeThemeName: StateFlow<String> = _activeThemeName.asStateFlow()

    private val _timerText = MutableStateFlow("02:00:00")
    val timerText: StateFlow<String> = _timerText.asStateFlow()
    private var countDownTimer: CountDownTimerWrapper? = null
    private val _reminderInterval = MutableStateFlow(2)
    val reminderInterval: StateFlow<Int> = _reminderInterval.asStateFlow()

    private val _unlockedAchievement = MutableStateFlow<Achievement?>(null)
    val unlockedAchievement: StateFlow<Achievement?> = _unlockedAchievement.asStateFlow()

    val dailyGoal: StateFlow<Int> = userPreferences.dailyGoalFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 8)

    private val _history = MutableStateFlow<List<WaterRecord>>(emptyList())
    val history: StateFlow<List<WaterRecord>> = _history.asStateFlow()

    private val _notificationHistory = MutableStateFlow<Set<String>>(emptySet())
    val notificationHistory: StateFlow<Set<String>> = _notificationHistory.asStateFlow()

    private val _achievements = MutableStateFlow(initialAchievements)
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                userPreferences.waterHistoryFlow,
                userPreferences.unlockedAchievementsFlow,
                userPreferences.notificationHistoryFlow,
                userPreferences.reminderIntervalFlow,
                userPreferences.lastCountDateFlow,
                userPreferences.waterCountFlow,
                userPreferences.userPointsFlow,
                userPreferences.purchasedItemsFlow,
                userPreferences.selectedThemeFlow
            ) { flows ->
                val history = flows[0] as List<WaterRecord>
                val unlockedTitles = flows[1] as Set<String>
                val notifications = flows[2] as Set<String>
                val reminderInterval = flows[3] as Int
                val lastDateStr = flows[4] as? String
                val savedCount = flows[5] as Int
                val points = flows[6] as Int
                val purchasedIds = flows[7] as Set<String>
                val activeTheme = flows[8] as String

                _history.value = history
                _achievements.update { current -> current.map { it.copy(isUnlocked = unlockedTitles.contains(it.title)) } }
                _notificationHistory.value = notifications
                _reminderInterval.value = reminderInterval
                _userPoints.value = points
                _activeThemeName.value = activeTheme

                if (lastDateStr == LocalDate.now().toString()) {
                    _currentWaterCount.value = savedCount
                } else {
                    _currentWaterCount.value = 0
                }

                _storeItems.update { items -> items.map { it.copy(isPurchased = purchasedIds.contains(it.id)) } }
            }.catch { e ->
                Log.e("WaterViewModel", "Error in init combine", e)
            }.launchIn(this)

            combine(currentWaterCount, dailyGoal, history, _achievements) { count, goal, historyList, achievements ->
                checkAchievements(count, goal, historyList, achievements)
            }.launchIn(this)
        }

        startOrResetTimer()
    }

    fun purchaseItem(itemId: String) {
        viewModelScope.launch {
            val item = _storeItems.value.find { it.id == itemId }
            if (item != null && !item.isPurchased && _userPoints.value >= item.cost) {
                val newPoints = _userPoints.value - item.cost
                _userPoints.value = newPoints
                userPreferences.saveUserPoints(newPoints)

                val currentPurchased = userPreferences.purchasedItemsFlow.first()
                val newPurchasedIds = currentPurchased + itemId
                userPreferences.savePurchasedItems(newPurchasedIds)

                _storeItems.update { currentItems ->
                    currentItems.map { if (it.id == itemId) it.copy(isPurchased = true) else it }
                }
            }
        }
    }

    fun setActiveTheme(themeName: String) {
        viewModelScope.launch {
            val item = _storeItems.value.find { it.id == themeName }
            if (themeName == "default" || (item != null && item.isPurchased)) {
                _activeThemeName.value = themeName
                userPreferences.saveSelectedTheme(themeName)
            }
        }
    }

    fun setReminderInterval(hours: Int) {
        viewModelScope.launch {
            userPreferences.saveReminderInterval(hours)
            _reminderInterval.value = hours
            startOrResetTimer()
        }
    }

    private fun startOrResetTimer() {
        cancelReminder()
        countDownTimer?.cancel()
        val intervalInMillis = TimeUnit.HOURS.toMillis(_reminderInterval.value.toLong())

        countDownTimer = timerProvider.create(intervalInMillis, 1000,
            onTick = { millisUntilFinished ->
                val h = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                val m = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                val s = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                _timerText.value = String.format("%02d:%02d:%02d", h, m, s)
            },
            onFinish = {
                _timerText.value = "00:00:00"
            }
        )
        countDownTimer?.start()

        scheduleReminder()
    }

    private fun scheduleReminder() {
        val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(_reminderInterval.value.toLong(), TimeUnit.HOURS)
            .addTag(REMINDER_WORK_TAG)
            .build()
        workManager.enqueue(reminderRequest)
    }

    private fun cancelReminder() {
        workManager.cancelAllWorkByTag(REMINDER_WORK_TAG)
    }

    fun addDrink(beverage: Beverage, glasses: Int) {
        viewModelScope.launch {
            val waterEquivalent = (glasses * beverage.hydrationFactor).roundToInt()
            val newCount = (_currentWaterCount.value + waterEquivalent).coerceAtLeast(0)
            _currentWaterCount.value = newCount
            userPreferences.saveWaterCount(newCount)

            val pointsToAdd = glasses * beverage.points
            val newPoints = (_userPoints.value + pointsToAdd).coerceAtLeast(0)
            _userPoints.value = newPoints
            userPreferences.saveUserPoints(newPoints)

            updateHistoryForDate(LocalDate.now(), newCount)
        }
        startOrResetTimer()
    }

    fun removeWater(glasses: Int) {
        viewModelScope.launch {
            val newCount = (_currentWaterCount.value - glasses).coerceAtLeast(0)
            _currentWaterCount.value = newCount
            userPreferences.saveWaterCount(newCount)
            updateHistoryForDate(LocalDate.now(), newCount)
        }
    }

    fun updateWaterCountForDate(date: LocalDate, newAmount: Int) {
        viewModelScope.launch {
            updateHistoryForDate(date, newAmount, true)
        }
    }

    private suspend fun updateHistoryForDate(date: LocalDate, amount: Int, forceUpdate: Boolean = false) {
        val newHistory = _history.value.toMutableList()
        val recordIndex = newHistory.indexOfFirst { it.date == date }

        if (recordIndex != -1) {
            newHistory[recordIndex] = newHistory[recordIndex].copy(amount = amount)
        } else {
            newHistory.add(WaterRecord(date, amount))
        }

        _history.value = newHistory.sortedBy { it.date }
        userPreferences.saveWaterHistory(newHistory)

        if (forceUpdate && date == LocalDate.now()) {
            _currentWaterCount.value = amount
            userPreferences.saveWaterCount(amount)
        }
    }


    private fun checkAchievements(currentAmount: Int, goal: Int, history: List<WaterRecord>, achievements: List<Achievement>) {
        val newlyUnlocked = mutableListOf<Achievement>()

        val newAchievements = achievements.map { achievement ->
            if (achievement.isUnlocked) return@map achievement

            val unlocked = when (achievement.title) {
                "Primer Vaso" -> currentAmount > 0
                "Meta Diaria" -> currentAmount >= goal
                "Súper Hidratado" -> currentAmount >= goal + 4
                "Maratón de Hidratación" -> currentAmount >= 15
                "Doble Meta" -> goal > 0 && currentAmount >= goal * 2
                "Maestro de la Hidratación" -> history.sumOf { it.amount } >= 200
                "Racha de 3 Días" -> checkStreak(history, goal, 3)
                "Semana Perfecta" -> checkStreak(history, goal, 7)
                else -> false
            }

            if (unlocked) {
                newlyUnlocked.add(achievement)
                achievement.copy(isUnlocked = true)
            } else {
                achievement
            }
        }

        if (newlyUnlocked.isNotEmpty()) {
            _unlockedAchievement.value = newlyUnlocked.first()
            viewModelScope.launch {
                val unlockedTitles = newAchievements.filter { it.isUnlocked }.map { it.title }.toSet()
                userPreferences.saveUnlockedAchievements(unlockedTitles)
                newlyUnlocked.forEach { achievement ->
                    val notificationMessage = "¡Logro desbloqueado: ${achievement.title}!"
                    userPreferences.addNotificationToHistory(notificationMessage)
                    _notificationHistory.update { it + notificationMessage }
                }
            }
        }

        _achievements.value = newAchievements
    }

    private fun checkStreak(history: List<WaterRecord>, goal: Int, streakDays: Int): Boolean {
        if (goal <= 0) return false
        val today = LocalDate.now()
        return (0 until streakDays).all {
            val dateToCheck = today.minusDays(it.toLong())
            history.any { record -> record.date == dateToCheck && record.amount >= goal }
        }
    }

    fun clearUnlockedAchievement() {
        _unlockedAchievement.value = null
    }

    suspend fun resetWaterCount() {
        userPreferences.clearAllData()
        _currentWaterCount.value = 0
        _userPoints.value = 0
        _storeItems.value = initialStoreItems
        _activeThemeName.value = "default"
        _history.value = emptyList()
        _notificationHistory.value = emptySet()
        _achievements.value = initialAchievements
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }

    companion object {
        const val REMINDER_WORK_TAG = "reminder"

        private val initialStoreItems = listOf(
            StoreItem("theme_forest", "Tema Bosque", "Tonos verdes para una experiencia natural.", 600),
            StoreItem("theme_ocean", "Tema Océano", "Relájate con los colores del mar.", 750),
            StoreItem("icon_sparkle", "Icono Brillante", "Un icono de vaso con destellos.", 1000)
        )

        private val initialAchievements = listOf(
            Achievement("Primer Vaso", "Bebiste tu primer vaso del día.", false),
            Achievement("Meta Diaria", "Alcanzaste tu meta diaria de hidratación.", false),
            Achievement("Súper Hidratado", "Superaste tu meta diaria por 4 vasos.", false),
            Achievement("Maratón de Hidratación", "Bebiste 15 vasos en un día.", false),
            Achievement("Doble Meta", "Bebiste el doble de tu meta diaria.", false),
            Achievement("Maestro de la Hidratación", "Has bebido un total de 200 vasos.", false),
            Achievement("Racha de 3 Días", "Cumpliste tu meta por 3 días seguidos.", false),
            Achievement("Semana Perfecta", "Cumpliste tu meta los 7 días de la semana.", false)
        )
    }
}
