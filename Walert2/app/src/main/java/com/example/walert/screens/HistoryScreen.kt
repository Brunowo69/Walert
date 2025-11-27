package com.example.walert.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.walert.viewmodel.WaterViewModel
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(
    navController: NavController,
    waterViewModel: WaterViewModel = viewModel()
) {
    val history by waterViewModel.history.collectAsState()
    val last7DaysHistory = history.takeLast(7)
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Historial de Consumo (Últimos 7 registros)",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (last7DaysHistory.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aún no hay registros.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(last7DaysHistory) { record ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(record.date.format(formatter))
                            // La referencia a 'amount' ahora funciona
                            Text("${record.amount} ml", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
