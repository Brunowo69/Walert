package com.example.walert.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.walert.datastore.UserPreferences
import com.example.walert.model.WeatherResponse
import com.example.walert.service.WeatherApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.roundToInt

// Define los estados por los que puede pasar la autenticación.
sealed interface AuthState {
    object Idle : AuthState
    object Loading : AuthState
    object Success : AuthState
    data class Error(val message: String) : AuthState
}

// Define los diferentes estados de la interfaz de usuario.
enum class GoalMode(val displayName: String) {
    MANUAL("Manual"),
    WEIGHT("Peso"),
    WEATHER("Clima")
}

sealed interface WeatherUiState {
    data class Success(val weather: WeatherResponse) : WeatherUiState
    object Error : WeatherUiState
    object Loading : WeatherUiState
}

data class ProfileScreenState(
    val alias: String = "",
    val dailyGoal: String = "8",
    val profileImageUri: String? = null,
    val weight: String = "0",
    val age: String = "0",
    val gender: String = "",
    val goalMode: GoalMode = GoalMode.MANUAL,
    val lastGoalSelectionDate: String? = null,
    val weatherState: WeatherUiState = WeatherUiState.Loading,
    val notificationsMuted: Boolean = false,
    val vibrationEnabled: Boolean = true,
    val isLoaded: Boolean = false // Nuevo campo para controlar el estado de carga
)

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferences = UserPreferences(application)
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(ProfileScreenState())
    val uiState: StateFlow<ProfileScreenState> = _uiState.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _saveComplete = MutableSharedFlow<Unit>()
    val saveComplete: SharedFlow<Unit> = _saveComplete.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                userPreferences.userAliasFlow,
                userPreferences.dailyGoalFlow,
                userPreferences.profileImageUriFlow,
                userPreferences.userWeightFlow,
                userPreferences.userAgeFlow,
                userPreferences.userGenderFlow,
                userPreferences.goalModeFlow,
                userPreferences.lastGoalSelectionDateFlow,
                userPreferences.notificationsMutedFlow,
                userPreferences.vibrationEnabledFlow
            ) { values ->
                val goalModeStr = values[6] as String
                val goalMode = try {
                    GoalMode.valueOf(goalModeStr.uppercase())
                } catch (e: Exception) {
                    GoalMode.MANUAL
                }

                ProfileScreenState(
                    alias = values[0] as String,
                    dailyGoal = (values[1] as Int).toString(),
                    profileImageUri = values[2] as String?,
                    weight = (values[3] as Int).toString(),
                    age = (values[4] as Int).toString(),
                    gender = values[5] as String,
                    goalMode = goalMode,
                    lastGoalSelectionDate = values[7] as String?,
                    notificationsMuted = values[8] as Boolean,
                    vibrationEnabled = values[9] as Boolean,
                    isLoaded = true // Marcamos como cargado
                )
            }.catch { e ->
                Log.e("UserViewModel", "Error in init combine", e)
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _authState.value = AuthState.Success
                    } else {
                        val error = when (task.exception) {
                            is FirebaseAuthInvalidUserException -> "No existe un usuario con ese correo."
                            is FirebaseAuthInvalidCredentialsException -> "La contraseña es incorrecta."
                            else -> "Error en el inicio de sesión. Inténtalo de nuevo."
                        }
                        _authState.value = AuthState.Error(error)
                    }
                }
        }
    }

    fun createUser(email: String, password: String, name: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Guardamos el nombre del nuevo usuario.
                        viewModelScope.launch {
                            onAliasChange(name)
                            saveProfileChanges()
                            _authState.value = AuthState.Success
                        }
                    } else {
                        val error = when (task.exception) {
                            is FirebaseAuthUserCollisionException -> "Este correo electrónico ya está registrado."
                            else -> "Error en el registro. Inténtalo de nuevo."
                        }
                        _authState.value = AuthState.Error(error)
                    }
                }
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
    
    // Funciones relacionadas con la obtención del clima.
    fun fetchWeatherByCity(city: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(weatherState = WeatherUiState.Loading) }
            try {
                val weather = WeatherApi.retrofitService.getWeather(city)
                _uiState.update { it.copy(weatherState = WeatherUiState.Success(weather)) }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error fetching weather by city: ${e.message}")
                _uiState.update { it.copy(weatherState = WeatherUiState.Error) }
            }
        }
    }

    fun fetchWeatherByCoordinates(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(weatherState = WeatherUiState.Loading) }
            try {
                val weather = WeatherApi.retrofitService.getWeatherByCoordinates(latitude, longitude)
                _uiState.update { it.copy(weatherState = WeatherUiState.Success(weather)) }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error fetching weather by coordinates: ${e.message}")
                _uiState.update { it.copy(weatherState = WeatherUiState.Error) }
            }
        }
    }

    // Funciones para modificar los datos del perfil.
    fun onAliasChange(newAlias: String) { _uiState.update { it.copy(alias = newAlias) } }
    fun onDailyGoalChange(newGoal: String) { _uiState.update { it.copy(dailyGoal = newGoal) } }
    fun onWeightChange(newWeight: String) { _uiState.update { it.copy(weight = newWeight) } }
    fun onAgeChange(newAge: String) { _uiState.update { it.copy(age = newAge) } }
    fun onGenderChange(newGender: String) { _uiState.update { it.copy(gender = newGender) } }

    fun onGoalModeChange(newMode: GoalMode) {
        _uiState.update { it.copy(goalMode = newMode) }
    }

    fun setMuteNotifications(isMuted: Boolean) {
        viewModelScope.launch {
            userPreferences.saveMuteNotifications(isMuted)
            _uiState.update { it.copy(notificationsMuted = isMuted) }
        }
    }

    fun setVibrationEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            userPreferences.saveVibrationEnabled(isEnabled)
            _uiState.update { it.copy(vibrationEnabled = isEnabled) }
        }
    }

    private suspend fun recalculateAndSaveGoal() {
        val state = _uiState.value
        val weightInKg = state.weight.toIntOrNull() ?: 0
        if (weightInKg <= 0 && state.goalMode != GoalMode.MANUAL) return

        val baseGlasses = ((weightInKg * 35) / 250.0).roundToInt()

        val finalGoal = when (val mode = state.goalMode) {
            GoalMode.MANUAL -> return // Este método no debería ser llamado si el modo es manual.
            GoalMode.WEIGHT -> baseGlasses
            GoalMode.WEATHER -> {
                if (state.weatherState is WeatherUiState.Success) {
                    val temp = state.weatherState.weather.main.temperature
                    val extraGlasses = if (temp > 25) 2 else if (temp > 20) 1 else 0
                    baseGlasses + extraGlasses
                } else {
                    baseGlasses // Si no podemos obtener el clima, calculamos la meta basándonos solo en el peso.
                }
            }
        }

        _uiState.update { it.copy(dailyGoal = finalGoal.toString()) }
        userPreferences.saveDailyGoal(finalGoal) // Guardamos el objetivo en las preferencias para que no se pierda.
    }

    fun onProfileImageChange(uri: Uri) {
        viewModelScope.launch {
            userPreferences.saveProfileImageUri(uri.toString())
            _uiState.update { it.copy(profileImageUri = uri.toString()) }
        }
    }

    fun triggerSaveProfileChanges() {
        viewModelScope.launch {
            saveProfileChanges()
            _saveComplete.emit(Unit)
        }
    }

    suspend fun saveProfileChanges() {
        val currentState = _uiState.value
        userPreferences.saveProfileData(
            alias = currentState.alias,
            dailyGoal = currentState.dailyGoal.toIntOrNull() ?: 8,
            weight = currentState.weight.toIntOrNull() ?: 0,
            age = currentState.age.toIntOrNull() ?: 0,
            gender = currentState.gender,
            goalMode = currentState.goalMode.name
        )
        recalculateAndSaveGoal()
    }

    suspend fun resetAllApplicationData(waterViewModel: WaterViewModel) {
        userPreferences.clearAllData()
        waterViewModel.resetWaterCount()
        _uiState.value = ProfileScreenState(isLoaded = false)
    }
}
