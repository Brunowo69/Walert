package com.example.walert.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.walert.R
import com.example.walert.components.ErrorSnackbar
import com.example.walert.components.SuccessSnackbar
import com.example.walert.navigation.AppScreens
import com.example.walert.viewmodel.AuthState
import com.example.walert.viewmodel.UserViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, userViewModel: UserViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val authState by userViewModel.authState.collectAsState()

    val isEmailValid = remember(email) { email.isNotBlank() && "@" in email }
    val isPasswordValid = remember(password) { password.length >= 6 }
    val isFormValid = isEmailValid && isPasswordValid

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(authState) {
        userViewModel.authState.collectLatest {
            when (it) {
                is AuthState.Success -> {
                    snackbarHostState.showSnackbar("SUCCESS:Inicio de sesión exitoso")
                    navController.navigate(AppScreens.MainScreen.route) { popUpTo(AppScreens.LoginScreen.route) { inclusive = true } }
                    userViewModel.resetAuthState() // Reset state after navigation
                }
                is AuthState.Error -> {
                    snackbarHostState.showSnackbar("ERROR:${it.message}")
                    userViewModel.resetAuthState()
                }
                else -> {}
            }
        }
    }

    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnimation = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF4915FC),
                        Color(0xFF42A2A0)
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { 
                SnackbarHost(snackbarHostState) { data ->
                    val message = data.visuals.message
                    when {
                        message.startsWith("SUCCESS:") -> SuccessSnackbar(data)
                        message.startsWith("ERROR:") -> ErrorSnackbar(data)
                        else -> Snackbar(data) // Default snackbar
                    }
                }
             }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = "Logo",
                    modifier = Modifier.size(100.dp),
                    tint = Color(0xFF03A9F4)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Bienvenido a Walert", style = MaterialTheme.typography.headlineLarge, color = Color.Black)

                Spacer(modifier = Modifier.height(8.dp))

                Text("Inicia sesión para continuar", style = MaterialTheme.typography.bodyLarge, color = Color.Black)

                Spacer(modifier = Modifier.height(32.dp))

                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                        disabledContainerColor = Color.Black.copy(alpha = 0.2f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.8f),
                        cursorColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Black) },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                        disabledContainerColor = Color.Black.copy(alpha = 0.2f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.8f),
                        cursorColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Black) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = Color.Black
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (isFormValid) {
                            userViewModel.signIn(email, password)
                        }
                    },
                    enabled = isFormValid && authState != AuthState.Loading,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.8f),
                        contentColor = Color(0xFF00529E)
                    )
                ) {
                    if (authState == AuthState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF00529E))
                    } else {
                        Text("Ingresar", style = MaterialTheme.typography.titleMedium)
                    }
                }

                TextButton(onClick = { navController.navigate(AppScreens.RegisterScreen.route) }) {
                    Text("¿No tienes cuenta? Regístrate", color = Color.Black)
                }
            }
        }
    }
}