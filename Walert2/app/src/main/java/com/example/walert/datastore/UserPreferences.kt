package com.example.walert.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.walert.viewmodel.WaterRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Serializable
private data class SerializableWaterRecord(val date: String, val amount: Int)

class UserPreferences(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val USER_ALIAS_KEY = stringPreferencesKey("user_alias")
        val DAILY_GOAL_KEY = intPreferencesKey("daily_goal")
        val PROFILE_IMAGE_URI_KEY = stringPreferencesKey("profile_image_uri")
        val USER_WEIGHT_KEY = intPreferencesKey("user_weight")
        val USER_AGE_KEY = intPreferencesKey("user_age")
        val USER_GENDER_KEY = stringPreferencesKey("user_gender")
        val GOAL_MODE_KEY = stringPreferencesKey("goal_mode")
        val LAST_GOAL_SELECTION_DATE_KEY = stringPreferencesKey("last_goal_selection_date")
        val REMINDER_INTERVAL_KEY = intPreferencesKey("reminder_interval")
        val NOTIFICATIONS_MUTED_KEY = booleanPreferencesKey("notifications_muted")
        val VIBRATION_ENABLED_KEY = booleanPreferencesKey("vibration_enabled")
        val REMINDER_DEADLINE_KEY = longPreferencesKey("reminder_deadline")
        val UNLOCKED_ACHIEVEMENTS_KEY = stringSetPreferencesKey("unlocked_achievements")
        val NOTIFICATION_HISTORY_KEY = stringSetPreferencesKey("notification_history")
        val WATER_COUNT_KEY = intPreferencesKey("water_count")
        val LAST_COUNT_DATE_KEY = stringPreferencesKey("last_count_date")
        val WATER_HISTORY_KEY = stringPreferencesKey("water_history")

        // Preferencias para los puntos del usuario y la tienda.
        val USER_POINTS_KEY = intPreferencesKey("user_points")
        val PURCHASED_ITEMS_KEY = stringSetPreferencesKey("purchased_items")

        // Preferencia para el tema que ha seleccionado el usuario.
        val SELECTED_THEME_KEY = stringPreferencesKey("selected_theme")
    }

    val userAliasFlow: Flow<String> = dataStore.data.map { it[USER_ALIAS_KEY] ?: "" }
    val dailyGoalFlow: Flow<Int> = dataStore.data.map { it[DAILY_GOAL_KEY] ?: 8 }
    val profileImageUriFlow: Flow<String?> = dataStore.data.map { it[PROFILE_IMAGE_URI_KEY] }
    val userWeightFlow: Flow<Int> = dataStore.data.map { it[USER_WEIGHT_KEY] ?: 0 }
    val userAgeFlow: Flow<Int> = dataStore.data.map { it[USER_AGE_KEY] ?: 0 }
    val userGenderFlow: Flow<String> = dataStore.data.map { it[USER_GENDER_KEY] ?: "" }
    val goalModeFlow: Flow<String> = dataStore.data.map { it[GOAL_MODE_KEY] ?: "Manual" }
    val lastGoalSelectionDateFlow: Flow<String?> = dataStore.data.map { it[LAST_GOAL_SELECTION_DATE_KEY] }
    val reminderIntervalFlow: Flow<Int> = dataStore.data.map { it[REMINDER_INTERVAL_KEY] ?: 2 }
    val notificationsMutedFlow: Flow<Boolean> = dataStore.data.map { it[NOTIFICATIONS_MUTED_KEY] ?: false }
    val vibrationEnabledFlow: Flow<Boolean> = dataStore.data.map { it[VIBRATION_ENABLED_KEY] ?: true }
    val reminderDeadlineFlow: Flow<Long> = dataStore.data.map { it[REMINDER_DEADLINE_KEY] ?: 0L }
    val unlockedAchievementsFlow: Flow<Set<String>> = dataStore.data.map { it[UNLOCKED_ACHIEVEMENTS_KEY] ?: emptySet() }
    val notificationHistoryFlow: Flow<Set<String>> = dataStore.data.map { it[NOTIFICATION_HISTORY_KEY] ?: emptySet() }
    val waterCountFlow: Flow<Int> = dataStore.data.map { it[WATER_COUNT_KEY] ?: 0 }
    val lastCountDateFlow: Flow<String?> = dataStore.data.map { it[LAST_COUNT_DATE_KEY] }

    // Flujos de datos para los puntos y la tienda.
    val userPointsFlow: Flow<Int> = dataStore.data.map { it[USER_POINTS_KEY] ?: 0 }
    val purchasedItemsFlow: Flow<Set<String>> = dataStore.data.map { it[PURCHASED_ITEMS_KEY] ?: emptySet() }

    // Flujo de datos para el tema seleccionado.
    val selectedThemeFlow: Flow<String> = dataStore.data.map { it[SELECTED_THEME_KEY] ?: "default" }


    val waterHistoryFlow: Flow<List<WaterRecord>> = dataStore.data.map { preferences ->
        val jsonString = preferences[WATER_HISTORY_KEY]
        if (jsonString.isNullOrEmpty()) {
            emptyList()
        } else {
            try {
                val serializableList = Json.decodeFromString<List<SerializableWaterRecord>>(jsonString)
                serializableList.map { WaterRecord(LocalDate.parse(it.date), it.amount) }
            } catch (e: Exception) {
                // Si hay un error, devolvemos una lista vacía para evitar que la app se rompa.
                emptyList()
            }
        }
    }

    suspend fun saveWaterHistory(history: List<WaterRecord>) {
        dataStore.edit { preferences ->
            val serializableList = history.map { SerializableWaterRecord(it.date.toString(), it.amount) }
            val jsonString = Json.encodeToString(serializableList)
            preferences[WATER_HISTORY_KEY] = jsonString
        }
    }

    suspend fun saveProfileData(
        alias: String, dailyGoal: Int, weight: Int, age: Int, gender: String, goalMode: String
    ) {
        dataStore.edit { preferences ->
            preferences[USER_ALIAS_KEY] = alias
            preferences[DAILY_GOAL_KEY] = dailyGoal
            preferences[USER_WEIGHT_KEY] = weight
            preferences[USER_AGE_KEY] = age
            preferences[USER_GENDER_KEY] = gender
            preferences[GOAL_MODE_KEY] = goalMode
            preferences[LAST_GOAL_SELECTION_DATE_KEY] = LocalDate.now().toString()
        }
    }

    suspend fun saveProfileImageUri(uri: String) {
        dataStore.edit {
            it[PROFILE_IMAGE_URI_KEY] = uri
        }
    }

    suspend fun saveDailyGoal(goal: Int) {
        dataStore.edit {
            it[DAILY_GOAL_KEY] = goal
        }
    }

    suspend fun saveReminderInterval(interval: Int) {
        dataStore.edit {
            it[REMINDER_INTERVAL_KEY] = interval
        }
    }

    suspend fun saveMuteNotifications(isMuted: Boolean) {
        dataStore.edit {
            it[NOTIFICATIONS_MUTED_KEY] = isMuted
        }
    }

    suspend fun saveVibrationEnabled(isEnabled: Boolean) {
        dataStore.edit {
            it[VIBRATION_ENABLED_KEY] = isEnabled
        }
    }

    suspend fun saveUnlockedAchievements(unlockedTitles: Set<String>) {
        dataStore.edit {
            it[UNLOCKED_ACHIEVEMENTS_KEY] = unlockedTitles
        }
    }

    suspend fun addNotificationToHistory(notificationText: String) {
        dataStore.edit {
            val currentHistory = it[NOTIFICATION_HISTORY_KEY] ?: emptySet()
            it[NOTIFICATION_HISTORY_KEY] = currentHistory + "${LocalDate.now()}: $notificationText"
        }
    }

    suspend fun saveReminderDeadline(deadline: Long) {
        dataStore.edit {
            it[REMINDER_DEADLINE_KEY] = deadline
        }
    }

    suspend fun saveWaterCount(count: Int) {
        dataStore.edit {
            it[WATER_COUNT_KEY] = count
            it[LAST_COUNT_DATE_KEY] = LocalDate.now().toString()
        }
    }

    // Funciones para guardar los puntos y los artículos comprados.
    suspend fun saveUserPoints(points: Int) {
        dataStore.edit {
            it[USER_POINTS_KEY] = points
        }
    }

    suspend fun savePurchasedItems(itemIds: Set<String>) {
        dataStore.edit {
            it[PURCHASED_ITEMS_KEY] = itemIds
        }
    }

    // Función para guardar el tema seleccionado.
    suspend fun saveSelectedTheme(themeName: String) {
        dataStore.edit {
            it[SELECTED_THEME_KEY] = themeName
        }
    }

    suspend fun clearAllData() {
        dataStore.edit { it.clear() }
    }
}