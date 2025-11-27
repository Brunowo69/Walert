package com.example.walert.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.walert.viewmodel.StoreItem
import com.example.walert.viewmodel.WaterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionScreen(
    navController: NavHostController,
    waterViewModel: WaterViewModel = viewModel()
) {
    val storeItems by waterViewModel.storeItems.collectAsState()
    val activeTheme by waterViewModel.activeThemeName.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var themeToActivate by remember { mutableStateOf<StoreItem?>(null) }

    val purchasedThemes = listOf(StoreItem("default", "Tema por Defecto", "El tema clásico de Walert", 0, true)) + 
                        storeItems.filter { it.isPurchased && it.id.startsWith("theme_") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Temas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(purchasedThemes) { theme ->
                ThemeItemCard(
                    theme = theme,
                    isActive = theme.id == activeTheme,
                    onClick = {
                        if (theme.id != activeTheme) { // Solo mostrar dialogo si no es el tema ya activo
                            themeToActivate = theme
                            showThemeDialog = true
                        }
                    }
                )
            }
        }
    }

    if (showThemeDialog && themeToActivate != null) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Cambiar Tema") },
            text = { Text("¿Quieres activar el tema '${themeToActivate!!.name}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        waterViewModel.setActiveTheme(themeToActivate!!.id)
                        showThemeDialog = false
                    }
                ) {
                    Text("Activar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ThemeItemCard(
    theme: StoreItem,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(theme.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(theme.description, style = MaterialTheme.typography.bodyMedium)
            }
            if (isActive) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Tema activo",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
