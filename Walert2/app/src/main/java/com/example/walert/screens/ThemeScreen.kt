@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.walert.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.walert.viewmodel.ThemeViewModel

@Composable
fun ThemeScreen(
    navController: NavHostController,
    themeViewModel: ThemeViewModel = viewModel()
) {
    val currentTheme by themeViewModel.themeMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cambiar tema") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("main") }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Tema actual: $currentTheme", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { themeViewModel.setThemeMode("Claro") }) {
                Text("Modo Claro")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = { themeViewModel.setThemeMode("Oscuro") }) {
                Text("Modo Oscuro")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = { themeViewModel.setThemeMode("Sistema") }) {
                Text("Usar tema del sistema")
            }
        }
    }
}
