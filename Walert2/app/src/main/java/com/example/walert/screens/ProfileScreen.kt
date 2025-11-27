package com.example.walert.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.walert.navigation.AppScreens
import com.example.walert.viewmodel.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel(),
) {
    val uiState by userViewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditAliasDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }

    LaunchedEffect(Unit) {
        userViewModel.saveComplete.collectLatest {
            showEditAliasDialog = false
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(it, flag)
                userViewModel.onProfileImageChange(it)
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            val imageModifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { imagePickerLauncher.launch("image/*") }

            val imageUri = uiState.profileImageUri
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = imageUri.toUri()),
                    contentDescription = "Foto de perfil",
                    modifier = imageModifier,
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = imageModifier, contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Icono de perfil",
                        modifier = Modifier.size(120.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                Text("Cambiar foto de perfil")
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.clickable { showEditAliasDialog = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = uiState.alias.ifBlank { "Añadir alias" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar alias",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            ProfileOptionRow(icon = Icons.Default.Settings, text = "Configuración", onClick = { navController.navigate(AppScreens.SettingsScreen.route) })
            ProfileOptionRow(icon = Icons.Default.Palette, text = "Mis Temas", onClick = { navController.navigate(AppScreens.ThemeSelectionScreen.route) })
            ProfileOptionRow(icon = Icons.Default.Notifications, text = "Historial de Notificaciones", onClick = { navController.navigate(AppScreens.NotificationsScreen.route) })
            ProfileOptionRow(icon = Icons.AutoMirrored.Filled.Help, text = "Ayuda y Soporte", onClick = { navController.navigate(AppScreens.HelpScreen.route) })
            ProfileOptionRow(icon = Icons.Default.Info, text = "Acerca de", onClick = { navController.navigate(AppScreens.AboutScreen.route) })

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar Sesión")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showEditAliasDialog) {
        EditAliasDialog(
            currentAlias = uiState.alias,
            onSave = { newAlias ->
                userViewModel.onAliasChange(newAlias)
                userViewModel.triggerSaveProfileChanges()
            },
            onDismiss = { showEditAliasDialog = false }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("¿Cerrar Sesión?") },
            text = { Text("Se cerrará tu sesión en este dispositivo.") },
            confirmButton = {
                Button(
                    onClick = {
                        auth.signOut()
                        navController.navigate(AppScreens.LoginScreen.route) { popUpTo(0) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ProfileOptionRow(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = text, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null)
    }
}

@Composable
fun EditAliasDialog(
    currentAlias: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var tempAlias by remember(currentAlias) { mutableStateOf(currentAlias) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar Alias") },
        text = {
            OutlinedTextField(
                value = tempAlias,
                onValueChange = { tempAlias = it },
                label = { Text("Nuevo alias") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(tempAlias) }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
