package com.example.walert.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.walert.viewmodel.WaterViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(navController: NavController, waterViewModel: WaterViewModel = viewModel()) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    val today = LocalDate.now()

    val history by waterViewModel.history.collectAsState()
    val dailyGoal by waterViewModel.dailyGoal.collectAsState()
    
    var noteText by remember { mutableStateOf("") }
    var waterCountText by remember { mutableStateOf("") }
    val notes = remember { mutableStateMapOf<LocalDate, String>() } 

    LaunchedEffect(selectedDate, history) {
        noteText = selectedDate?.let { notes[it] } ?: ""
        waterCountText = history.find { it.date == selectedDate }?.amount?.toString() ?: "0"
    }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendario de Progreso") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val monthName = currentMonth.month.getDisplayName(TextStyle.FULL, Locale("es", "ES")).replaceFirstChar { it.titlecase() }

            Spacer(modifier = Modifier.height(16.dp))

            // Month and Year Navigation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Mes Anterior")
                }
                Text(
                    text = "$monthName ${currentMonth.year}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Mes Siguiente")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Calendar Grid
            Column {
                val daysOfWeek = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
                Row(modifier = Modifier.fillMaxWidth()) {
                    daysOfWeek.forEach {
                        Text(text = it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                val firstDayOffset = currentMonth.atDay(1).dayOfWeek.value - 1
                val daysInMonth = currentMonth.lengthOfMonth()
                val totalCells = (daysInMonth + firstDayOffset).let { if (it % 7 == 0) it else it + (7 - it % 7) }

                for (week in 0 until (totalCells / 7)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (day in 1..7) {
                            val dayOfMonth = (week * 7) + day - firstDayOffset
                            Box(modifier = Modifier.weight(1f).padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                                if (dayOfMonth > 0 && dayOfMonth <= daysInMonth) {
                                    val date = currentMonth.atDay(dayOfMonth)
                                    val isSelected = selectedDate == date
                                    val isToday = today == date
                                    val record = history.find { it.date == date }
                                    val goalMet = record != null && record.amount >= dailyGoal
                                    val partialProgress = record != null && record.amount > 0 && !goalMet

                                    val targetColor = when {
                                        isSelected -> MaterialTheme.colorScheme.secondary // A distinct color for selection
                                        goalMet -> MaterialTheme.colorScheme.primary
                                        partialProgress -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        else -> Color.Transparent
                                    }
                                    
                                    val backgroundColor by animateColorAsState(targetColor, label = "DayBackground")
                                    val textColor by animateColorAsState(if (isSelected || goalMet || partialProgress) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, label = "DayText")

                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(backgroundColor)
                                            .then(if (isToday && !isSelected) Modifier.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
                                            .clickable { selectedDate = date },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = dayOfMonth.toString(), color = textColor, fontWeight = if(goalMet || partialProgress) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            if (selectedDate != null) {
                Column { 
                    val canEditDate = !selectedDate!!.isAfter(today)
                    val selectedMonthName = selectedDate?.month?.getDisplayName(TextStyle.FULL, Locale("es", "ES"))?.replaceFirstChar { it.titlecase() } ?: monthName

                    // Water Intake Edit Section
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Editar Consumo del Día", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = waterCountText,
                            onValueChange = { waterCountText = it },
                            label = { Text("Vasos de agua") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = canEditDate
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val newAmount = waterCountText.toIntOrNull() ?: 0
                                selectedDate?.let { waterViewModel.updateWaterCountForDate(it, newAmount) }
                                scope.launch { snackbarHostState.showSnackbar("Consumo actualizado") }
                            },
                            modifier = Modifier.align(Alignment.End),
                            enabled = canEditDate
                        ) {
                            Text("Guardar Consumo")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Notes Section (can always be edited)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Notas del ${selectedDate?.dayOfMonth} de $selectedMonthName", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            label = { Text("Escribe tus pensamientos...") },
                            modifier = Modifier.fillMaxWidth().height(120.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                selectedDate?.let { notes[it] = noteText }
                                scope.launch { snackbarHostState.showSnackbar("Nota guardada") }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Guardar Nota")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
