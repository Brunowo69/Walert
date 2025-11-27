package com.example.walert.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import com.example.walert.datastore.UserPreferences

class WaterViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WaterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WaterViewModel(
                userPreferences = UserPreferences(application),
                workManager = WorkManager.getInstance(application),
                timerProvider = AndroidTimerProvider()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}