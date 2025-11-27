package com.example.walert.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.waterStore by preferencesDataStore("water_prefs")

class WaterPreferences(private val context: Context) {
    companion object {
        val GLASSES = intPreferencesKey("glasses")
    }

    fun getWater() = context.waterStore.data.map { prefs ->
        prefs[GLASSES] ?: 0
    }

    suspend fun saveWater(value: Int) {
        context.waterStore.edit { prefs ->
            prefs[GLASSES] = value
        }
    }
}
