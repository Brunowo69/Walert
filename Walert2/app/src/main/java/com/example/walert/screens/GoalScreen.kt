package com.example.walert.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.* 
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.walert.navigation.AppScreens
import com.example.walert.viewmodel.GoalMode
import com.example.walert.viewmodel.UserViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel()
) {
    val uiState by userViewModel.uiState.collectAsState()
    val isGoalSetForToday = uiState.lastGoalSelectionDate == LocalDate.now().toString()

    // Si la meta no está definida para hoy, desactivamos el botón de retroceso.
    BackHandler(enabled = !isGoalSetForToday) {}

    LaunchedEffect(Unit) {
        userViewModel.saveComplete.collectLatest {
            // Cuando el guardado se complete, navegamos.
            if (!isGoalSetForToday) {
                navController.navigate(AppScreens.MainScreen.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            } else {
                navController.popBackStack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Establecer Meta Diaria") },
                navigationIcon = {
                    // Solo mostramos el icono de "volver" si la meta ya está definida.
                    if (isGoalSetForToday) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isGoalSetForToday) "Ajusta tu meta diaria" else "Elige cómo quieres definir tu meta diaria.",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                GoalMode.entries.forEachIndexed { index, goalMode ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = GoalMode.entries.size),
                        onClick = { userViewModel.onGoalModeChange(goalMode) },
                        selected = uiState.goalMode == goalMode,
                        enabled = !isGoalSetForToday // Desactivamos el botón si la meta ya se ha establecido.
                    ) {
                        Text(goalMode.displayName)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (uiState.goalMode) {
                GoalMode.MANUAL -> {
                    OutlinedTextField(
                        value = uiState.dailyGoal,
                        onValueChange = { userViewModel.onDailyGoalChange(it) },
                        label = { Text("Meta (en vasos)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                GoalMode.WEIGHT -> {
                    Text(
                        text = "La meta se calculará según tu peso. Asegúrate de tenerlo actualizado en tu perfil.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                GoalMode.WEATHER -> {
                    Text(
                        text = "La meta se ajustará según el clima de tu ubicación actual.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { userViewModel.triggerSaveProfileChanges() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Guardar Meta")
            }
        }
    }
}
