package com.example.walert.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.walert.navigation.AppScreens
import com.example.walert.viewmodel.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    waterViewModel: WaterViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val count by waterViewModel.currentWaterCount.collectAsState()
    val userState by userViewModel.uiState.collectAsState()
    val goal = userState.dailyGoal.toIntOrNull() ?: 8
    val progress = if (goal > 0) count.toFloat() / goal.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "Progress Animation",
        animationSpec = tween(1000)
    )
    val timerText by waterViewModel.timerText.collectAsState()
    val unlockedAchievement by waterViewModel.unlockedAchievement.collectAsState()
    var showBeverageDialog by remember { mutableStateOf(false) }
    var showRemoveWaterDialog by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Walert - Control de Agua") },
                    actions = {
                        IconButton(onClick = { navController.navigate(AppScreens.CalendarScreen.route) }) {
                            Icon(Icons.Filled.CalendarToday, contentDescription = "Calendario")
                        }
                        IconButton(onClick = { navController.navigate(AppScreens.AchievementsScreen.route) }) {
                            Icon(Icons.Filled.EmojiEvents, contentDescription = "Logros")
                        }
                        IconButton(onClick = { navController.navigate(AppScreens.ProfileScreen.route) }) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = "Perfil")
                        }
                    }
                )
            },
            floatingActionButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FloatingActionButton(
                        onClick = { showRemoveWaterDialog = true },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Quitar Vaso")
                    }
                    FloatingActionButton(
                        onClick = { showBeverageDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar Bebida")
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.Center
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    // --- Weather Section ---
                    WeatherSection(userState.weatherState)

                    // --- Timer Section ---
                    TimerSection(timerText)

                    // --- Water Tracking Section ---
                    WaterTrackingSection(count, goal, animatedProgress)

                    // --- Quick Actions ---
                    QuickActionsSection(navController)

                    // --- Fun Fact Section ---
                    FunFactSection()
                }
                Spacer(modifier = Modifier.height(80.dp)) // Spacer for FAB
            }
        }

        AchievementUnlockedPopup(
            achievement = unlockedAchievement,
            onDismiss = { waterViewModel.clearUnlockedAchievement() }
        )

        if (showBeverageDialog) {
            BeverageSelectionDialog(
                onBeverageSelected = { beverage ->
                    waterViewModel.addDrink(beverage, 1)
                    showBeverageDialog = false
                },
                onDismiss = { showBeverageDialog = false }
            )
        }

        if (showRemoveWaterDialog) {
            RemoveWaterDialog(
                onConfirm = {
                    waterViewModel.removeWater(1)
                    showRemoveWaterDialog = false
                },
                onDismiss = { showRemoveWaterDialog = false }
            )
        }
    }
}

@Composable
private fun WeatherSection(weatherState: WeatherUiState) {
    when (weatherState) {
        is WeatherUiState.Loading -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
        is WeatherUiState.Success -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = weatherState.weather.cityName, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "${weatherState.weather.main.temperature.roundToInt()}°C", style = MaterialTheme.typography.titleLarge)
            }
        }
        is WeatherUiState.Error -> Text("Clima no disponible")
    }
}

@Composable
private fun TimerSection(timerText: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Siguiente recordatorio en:", style = MaterialTheme.typography.titleMedium)
        Text(timerText, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun WaterTrackingSection(count: Int, goal: Int, progress: Float) {
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.size(200.dp),
            strokeWidth = 16.dp,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Has bebido")
            AnimatedContent(targetState = count, label = "Water Count Animation") {
                Text(text = "$it vasos", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            }
            Text(text = "Meta: $goal vasos", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun QuickActionsSection(navController: NavHostController) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Acciones Rápidas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ActionCard(title = "Tienda", icon = Icons.Default.Store, modifier = Modifier.weight(1f)) {
                navController.navigate(AppScreens.StoreScreen.route)
            }
            ActionCard(title = "Estadísticas", icon = Icons.Default.BarChart, modifier = Modifier.weight(1f)) { navController.navigate(AppScreens.StatisticsScreen.route) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionCard(title: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

private val waterFacts = listOf(
    "El 75% del cerebro humano es agua.",
    "El agua regula la temperatura de la Tierra y del cuerpo humano.",
    "Una persona puede sobrevivir un mes sin comida, pero solo una semana sin agua.",
    "El 70% de la superficie de la Tierra está cubierta de agua.",
    "Menos del 1% del agua del mundo es potable.",
    "Beber agua puede ayudar a prevenir dolores de cabeza.",
    "El agua ayuda a transportar nutrientes y oxígeno a las células.",
    "Un cuerpo deshidratado puede experimentar calambres musculares.",
    "El agua es el único elemento que se encuentra en la naturaleza en tres formas: sólido, líquido y gaseoso.",
    "Los huesos humanos son un 25% agua.",
    "Beber suficiente agua puede mejorar el estado de ánimo.",
    "El agua ayuda a eliminar las toxinas del cuerpo.",
    "La Antártida tiene aproximadamente el 90% de todo el hielo del mundo.",
    "Una gota de agua puede albergar millones de microorganismos.",
    "El agua es esencial para la digestión y la absorción de nutrientes.",
    "Sentir sed ya es un síntoma de deshidratación.",
    "El agua potable en las ciudades es reciclada constantemente."
)

@Composable
private fun FunFactSection() {
    val randomFact by remember { mutableStateOf(waterFacts.random()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lightbulb, contentDescription = "Dato Curioso")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Dato Curioso", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = randomFact, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun BeverageSelectionDialog(
    onBeverageSelected: (Beverage) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = MaterialTheme.shapes.large) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("¿Qué has bebido?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(Beverage.values()) { beverage ->
                        BeverageItem(beverage = beverage, onClick = { onBeverageSelected(beverage) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BeverageItem(beverage: Beverage, onClick: () -> Unit) {
    val icon = when (beverage) {
        Beverage.WATER -> Icons.Outlined.WaterDrop
        Beverage.TEA -> Icons.Outlined.EmojiFoodBeverage
        Beverage.COFFEE -> Icons.Outlined.Coffee
        Beverage.JUICE -> Icons.Outlined.LocalBar
        Beverage.SODA -> Icons.Outlined.Fastfood
        Beverage.ENERGY_DRINK -> Icons.Outlined.Bolt
    }
    val pointsColor = if (beverage.points >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.height(160.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = beverage.displayName, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = beverage.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${beverage.points} pts",
                color = pointsColor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RemoveWaterDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quitar Vaso") },
        text = { Text("¿Estás seguro de que quieres quitar un vaso?") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
