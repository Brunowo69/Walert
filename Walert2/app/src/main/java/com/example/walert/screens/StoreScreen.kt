package com.example.walert.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Savings
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
fun StoreScreen(
    navController: NavHostController,
    waterViewModel: WaterViewModel = viewModel()
) {
    val userPoints by waterViewModel.userPoints.collectAsState()
    val storeItems by waterViewModel.storeItems.collectAsState()

    var showPurchaseDialog by remember { mutableStateOf(false) }
    var itemToPurchase by remember { mutableStateOf<StoreItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tienda de Gotas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Icon(Icons.Default.Savings, contentDescription = "Puntos")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "$userPoints", fontWeight = FontWeight.Bold)
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
            items(storeItems) { item ->
                StoreItemCard(item = item, userPoints = userPoints) {
                    itemToPurchase = item
                    showPurchaseDialog = true
                }
            }
        }
    }

    if (showPurchaseDialog && itemToPurchase != null) {
        AlertDialog(
            onDismissRequest = { showPurchaseDialog = false },
            title = { Text("Confirmar Compra") },
            text = { Text("¿Seguro que quieres comprar '${itemToPurchase!!.name}' por ${itemToPurchase!!.cost} Gotas?") },
            confirmButton = {
                Button(
                    onClick = {
                        waterViewModel.purchaseItem(itemToPurchase!!.id)
                        showPurchaseDialog = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurchaseDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun StoreItemCard(
    item: StoreItem,
    userPoints: Int,
    onPurchaseClick: () -> Unit
) {
    val canAfford = userPoints >= item.cost

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(item.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${item.cost} Gotas", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Button(
                    onClick = onPurchaseClick,
                    enabled = !item.isPurchased && canAfford
                ) {
                    Text(if (item.isPurchased) "Comprado" else "Comprar")
                }
            }
        }
    }
}
