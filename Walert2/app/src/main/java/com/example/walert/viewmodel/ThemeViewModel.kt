package com.example.walert.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Este ViewModel se encarga de gestionar el tema de la aplicación (Claro, Oscuro o el del Sistema).
class ThemeViewModel : ViewModel() {

    // Un StateFlow para guardar el modo del tema actual. Por defecto, usamos el del sistema.
    private val _themeMode = MutableStateFlow("Sistema")
    val themeMode = _themeMode.asStateFlow()

    // Esta función nos permite cambiar el modo del tema.
    fun setThemeMode(mode: String) {
        _themeMode.value = mode
    }

    // Esta función nos devuelve el modo del tema actual.
    fun getThemeMode(): String = _themeMode.value
}
