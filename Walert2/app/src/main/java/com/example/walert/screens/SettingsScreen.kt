package com.example.walert.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.walert.viewmodel.GoalMode
import com.example.walert.viewmodel.UserViewModel
import com.example.walert.viewmodel.WaterViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, userViewModel: UserViewModel = viewModel(), waterViewModel: WaterViewModel = viewModel()) {
    val uiState by userViewModel.uiState.collectAsState()
    val waterState by waterViewModel.reminderInterval.collectAsState()

    var selectedInterval by remember(waterState) { mutableStateOf(waterState) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.padding(12.dp),
                    shape = RoundedCornerShape(8.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Éxito", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = data.visuals.message, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- Notification Settings ---
            Text("Notificaciones", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Silenciar Notificaciones", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = uiState.notificationsMuted,
                    onCheckedChange = { userViewModel.setMuteNotifications(it) }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Vibración en Notificaciones", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = uiState.vibrationEnabled,
                    onCheckedChange = { userViewModel.setVibrationEnabled(it) }
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // --- Reminder Interval Selector ---
            Text("Intervalo de Recordatorio (horas)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            val reminderOptions = listOf(1, 2, 3)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                reminderOptions.forEachIndexed { index, hours ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = reminderOptions.size),
                        onClick = { selectedInterval = hours },
                        selected = selectedInterval == hours
                    ) {
                        Text("$hours")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- Health Data ---
            Text("Datos de Salud", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.weight,
                    onValueChange = userViewModel::onWeightChange,
                    label = { Text("Peso (kg)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = uiState.age,
                    onValueChange = userViewModel::onAgeChange,
                    label = { Text("Edad") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            val genderOptions = listOf("Masculino", "Femenino", "Otro")
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                genderOptions.forEachIndexed { index, gender ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = genderOptions.size),
                        onClick = { userViewModel.onGenderChange(gender) },
                        selected = uiState.gender == gender
                    ) {
                        Text(gender)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // --- Goal Mode Selector ---
            val isWeightEntered = uiState.weight.toIntOrNull() ?: 0 > 0
            Text("Modo de Meta", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                GoalMode.values().forEachIndexed { index, goalMode ->
                    val isEnabled = when (goalMode) {
                        GoalMode.MANUAL -> true
                        GoalMode.WEIGHT, GoalMode.WEATHER -> isWeightEntered
                    }
                    val text = when (goalMode) {
                        GoalMode.MANUAL -> "Manual"
                        GoalMode.WEIGHT -> "Peso/Edad"
                        GoalMode.WEATHER -> "Clima"
                    }
                    SegmentedButton(
                        enabled = isEnabled,
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = GoalMode.values().size),
                        onClick = { userViewModel.onGoalModeChange(goalMode) },
                        selected = uiState.goalMode == goalMode,
                        icon = { 
                            if (uiState.goalMode == goalMode) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                            }
                        }
                    ) {
                        Text(text)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- Daily Goal Field ---
            OutlinedTextField(
                value = uiState.dailyGoal,
                onValueChange = userViewModel::onDailyGoalChange,
                label = { Text("Meta Diaria (vasos)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.goalMode == GoalMode.MANUAL,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null) }
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            // --- Save Button ---
            Button(
                onClick = {
                    scope.launch {
                        userViewModel.saveProfileChanges()
                        waterViewModel.setReminderInterval(selectedInterval)
                        snackbarHostState.showSnackbar("¡Cambios guardados con éxito!")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = "Guardar")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar Configuración")
            }
        }
    }
}
