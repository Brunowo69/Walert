package com.example.walert.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.walert.viewmodel.StatisticsState
import com.example.walert.viewmodel.StatisticsViewModel
import com.example.walert.viewmodel.WeeklyData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(navController: NavController, statisticsViewModel: StatisticsViewModel = viewModel()) {
    val uiState by statisticsViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas de Consumo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WeeklyConsumptionChart(uiState)
            Spacer(modifier = Modifier.height(24.dp))
            StatisticsCards(uiState)
        }
    }
}

@Composable
private fun WeeklyConsumptionChart(state: StatisticsState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Consumo Semanal (vasos)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        WeeklyBarChart(data = state.weeklyConsumption)
    }
}

@Composable
fun WeeklyBarChart(data: List<WeeklyData>) {
    val maxAmount = data.maxOfOrNull { it.amount } ?: 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Bottom
    ) {
        val barColor = MaterialTheme.colorScheme.primary

        data.forEach { weeklyData ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = weeklyData.amount.toString(), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Canvas(modifier = Modifier.width(24.dp).height(150.dp)) {
                    val barHeight = (weeklyData.amount.toFloat() / maxAmount) * 150.dp.toPx()
                    drawLine(
                        color = barColor,
                        start = Offset(x = center.x, y = 150.dp.toPx()),
                        end = Offset(x = center.x, y = 150.dp.toPx() - barHeight),
                        strokeWidth = 24.dp.toPx()
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = weeklyData.day, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun StatisticsCards(state: StatisticsState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatisticInfoCard(
                icon = Icons.Default.Whatshot,
                label = "Racha Actual",
                value = "${state.streak} días",
                modifier = Modifier.weight(1f)
            )
            StatisticInfoCard(
                icon = Icons.Default.TrendingUp,
                label = "Promedio Diario",
                value = "${String.format("%.1f", state.dailyAverage)} vasos",
                modifier = Modifier.weight(1f)
            )
        }
        StatisticInfoCard(
            icon = Icons.Default.Star,
            label = "Mejor Día",
            value = "${state.bestDay} vasos",
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Composable
fun StatisticInfoCard(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onTertiaryContainer)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onTertiaryContainer, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer, textAlign = TextAlign.Center)
        }
    }
}
