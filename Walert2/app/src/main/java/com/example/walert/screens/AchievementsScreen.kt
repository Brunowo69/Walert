@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.walert.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.walert.model.Achievement
import com.example.walert.viewmodel.WaterViewModel

@Composable
fun AchievementsScreen(
    navController: NavHostController,
    waterViewModel: WaterViewModel
) {
    val achievements by waterViewModel.achievements.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Logros") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            items(achievements) { ach ->
                AchievementCard(ach)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun AchievementCard(achievement: Achievement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (achievement.isUnlocked)
            CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer)
        else
            CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(achievement.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                if (achievement.isUnlocked) achievement.description else "Logro bloqueado…",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
