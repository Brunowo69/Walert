package com.example.walert

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.walert.navigation.AppScreens
import com.example.walert.screens.*
import com.example.walert.ui.theme.WalertTheme
import com.example.walert.viewmodel.UserViewModel
import com.example.walert.viewmodel.WaterViewModel
import com.example.walert.viewmodel.WaterViewModelFactory
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WalertApp()
        }
    }
}

@Composable
fun WalertApp() {
    val userViewModel: UserViewModel = viewModel()
    val context = LocalContext.current
    val waterViewModel: WaterViewModel = viewModel(factory = WaterViewModelFactory(context.applicationContext as Application))
    val activeTheme by waterViewModel.activeThemeName.collectAsState()
    val userState by userViewModel.uiState.collectAsState()

    var showGoalDialog by remember { mutableStateOf(false) }
    val navController = rememberNavController()
    val auth = remember { FirebaseAuth.getInstance() }
    val scope = rememberCoroutineScope()

    // Efecto para validar la sesión del usuario al iniciar la app.
    LaunchedEffect(auth.currentUser) {
        auth.currentUser?.reload()?.addOnFailureListener {
            // Si el usuario fue eliminado en Firebase, cerramos la sesión y limpiamos los datos.
            scope.launch {
                userViewModel.resetAllApplicationData(waterViewModel)
                auth.signOut()
                navController.navigate(AppScreens.LoginScreen.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        }
    }

    LaunchedEffect(userState.isLoaded, userState.lastGoalSelectionDate) {
        if (userState.isLoaded) {
            val today = LocalDate.now().toString()
            // Solo mostramos el diálogo si el usuario ha iniciado sesión.
            if (auth.currentUser != null && userState.lastGoalSelectionDate != today) {
                showGoalDialog = true
            }
        }
    }

    WalertTheme(themeName = activeTheme) {
        val startDestination = if (auth.currentUser != null) AppScreens.MainScreen.route else AppScreens.LoginScreen.route

        if (!userState.isLoaded) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (showGoalDialog) {
                AlertDialog(
                    onDismissRequest = { /* No hacer nada para que sea obligatorio */ },
                    title = { Text("Establece tu meta para hoy") },
                    text = { Text("Para continuar, necesitas definir tu meta de hidratación para el día de hoy.") },
                    confirmButton = {
                        Button(onClick = { 
                            navController.navigate(AppScreens.GoalScreen.route)
                            showGoalDialog = false 
                        }) {
                            Text("Establecer meta")
                        }
                    },
                    dismissButton = null // Eliminamos el botón para que no se pueda descartar.
                )
            }

            // Gestionamos los permisos necesarios para la app.
            val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS)
            } else {
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            }

            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissionsResult ->
                if (permissionsResult.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            if (location != null) {
                                userViewModel.fetchWeatherByCoordinates(location.latitude, location.longitude)
                            }
                        }
                    }
                } else {
                    // Si no tenemos permiso de ubicación, usamos una ciudad por defecto.
                    userViewModel.fetchWeatherByCity("Santiago")
                }
            }

            LaunchedEffect(Unit) {
                launcher.launch(permissionsToRequest)
            }

            AnimatedVisibility(visible = userState.isLoaded) {
                AppNavigation(navController, startDestination, userViewModel, waterViewModel)
            }
        }
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String,
    userViewModel: UserViewModel,
    waterViewModel: WaterViewModel
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(AppScreens.LoginScreen.route) { LoginScreen(navController = navController, userViewModel = userViewModel) }
        composable(AppScreens.RegisterScreen.route) { RegisterScreen(navController = navController, userViewModel = userViewModel) }
        composable(AppScreens.MainScreen.route) { MainScreen(navController = navController, waterViewModel = waterViewModel, userViewModel = userViewModel) }
        composable(AppScreens.ProfileScreen.route) { ProfileScreen(navController = navController, userViewModel = userViewModel) }
        composable(AppScreens.SettingsScreen.route) { SettingsScreen(navController = navController, userViewModel = userViewModel, waterViewModel = waterViewModel) }
        composable(AppScreens.AchievementsScreen.route) { AchievementsScreen(navController = navController, waterViewModel = waterViewModel) }
        composable(AppScreens.CalendarScreen.route) { CalendarScreen(navController = navController, waterViewModel = waterViewModel) }
        composable(AppScreens.ThemeScreen.route) { ThemeScreen(navController = navController) }
        composable(AppScreens.GoalScreen.route) { GoalScreen(navController = navController, userViewModel = userViewModel) }
        composable(AppScreens.HistoryScreen.route) { HistoryScreen(navController = navController) }
        composable(AppScreens.NotificationsScreen.route) { NotificationsScreen(navController = navController, waterViewModel = waterViewModel) }
        composable(AppScreens.HelpScreen.route) { HelpScreen(navController = navController) }
        composable(AppScreens.AboutScreen.route) { AboutScreen(navController = navController) }
        composable(AppScreens.RemindersScreen.route) { RemindersScreen(navController = navController) }
        composable(AppScreens.StatisticsScreen.route) { StatisticsScreen(navController = navController) }
        composable(AppScreens.StoreScreen.route) { StoreScreen(navController = navController, waterViewModel = waterViewModel) }
        composable(AppScreens.ThemeSelectionScreen.route) { ThemeSelectionScreen(navController = navController, waterViewModel = waterViewModel) }
    }
}