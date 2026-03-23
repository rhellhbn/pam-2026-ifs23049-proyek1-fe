package org.delcom.pam_proyek1_ifs23049.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.delcom.pam_proyek1_ifs23049.helper.*
import org.delcom.pam_proyek1_ifs23049.ui.viewmodels.AuthActionUIState
import org.delcom.pam_proyek1_ifs23049.ui.viewmodels.AuthViewModel

@Composable
fun AuthRegisterScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    authViewModel: AuthViewModel
) {
    val uiState by authViewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var hasSubmitted by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // isLoading hanya true setelah tombol ditekan
    val isLoading = hasSubmitted && uiState.authRegister is AuthActionUIState.Loading

    LaunchedEffect(uiState.authRegister) {
        if (!hasSubmitted) return@LaunchedEffect
        when (val state = uiState.authRegister) {
            is AuthActionUIState.Success -> {
                coroutineScope.launch {
                    SuspendHelper.showSnackBar(
                        snackbarHost,
                        SuspendHelper.SnackBarType.SUCCESS,
                        "Registrasi berhasil! Silahkan login."
                    )
                }
                RouteHelper.back(navController)
            }
            is AuthActionUIState.Error -> {
                coroutineScope.launch {
                    SuspendHelper.showSnackBar(
                        snackbarHost,
                        SuspendHelper.SnackBarType.ERROR,
                        state.message
                    )
                }
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Buat Akun", style = MaterialTheme.typography.headlineMedium)
        Text("Daftarkan akun baru Anda", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama Lengkap") },
            leadingIcon = { Icon(Icons.Default.Badge, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        if (showPassword) Icons.Default.VisibilityOff
                        else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None
            else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (!isLoading) {
                    hasSubmitted = true
                    authViewModel.register(name, username, password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && name.isNotBlank() && username.isNotBlank() && password.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Register")
            }
        }
        Spacer(Modifier.height(8.dp))

        TextButton(onClick = { RouteHelper.back(navController) }) {
            Text("Sudah punya akun? Login")
        }
    }
}