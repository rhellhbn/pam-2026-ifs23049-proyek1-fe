package org.delcom.pam_proyek1_ifs23049.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.delcom.pam_proyek1_ifs23049.helper.*
import org.delcom.pam_proyek1_ifs23049.ui.components.*
import org.delcom.pam_proyek1_ifs23049.ui.viewmodels.*

@Composable
fun ProfileScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    libraryViewModel: LibraryViewModel
) {
    val authState by authViewModel.uiState.collectAsState()
    val uiState by libraryViewModel.uiState.collectAsState()
    val authToken = (authState.auth as? AuthUIState.Success)?.data?.authToken ?: ""
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var isInitialized by remember { mutableStateOf(false) }
    var hasSubmittedProfile by remember { mutableStateOf(false) }
    var hasSubmittedPassword by remember { mutableStateOf(false) }

    LaunchedEffect(authToken) {
        if (authToken.isNotEmpty()) libraryViewModel.getProfile(authToken)
    }

    LaunchedEffect(uiState.profile) {
        if (!isInitialized && uiState.profile is ProfileUIState.Success) {
            val data = (uiState.profile as ProfileUIState.Success).data
            name = data.name
            username = data.username
            isInitialized = true
        }
    }

    // Redirect ke login saat auth jadi Error (setelah logout)
    LaunchedEffect(authState.auth) {
        if (authState.auth is AuthUIState.Error) {
            RouteHelper.to(
                navController,
                ConstHelper.RouteNames.AuthLogin.path,
                removeBackStack = true
            )
        }
    }

    LaunchedEffect(uiState.userChange) {
        if (!hasSubmittedProfile) return@LaunchedEffect
        when (val state = uiState.userChange) {
            is BookActionUIState.Success -> {
                coroutineScope.launch {
                    SuspendHelper.showSnackBar(
                        snackbarHost = SnackbarHostState(),
                        type = SuspendHelper.SnackBarType.SUCCESS,
                        message = "Profil berhasil diperbarui!"
                    )
                }
                libraryViewModel.getProfile(authToken)
            }
            is BookActionUIState.Error -> {
                coroutineScope.launch {
                    SuspendHelper.showSnackBar(
                        snackbarHost = SnackbarHostState(),
                        type = SuspendHelper.SnackBarType.ERROR,
                        message = state.message
                    )
                }
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = "Profil",
                actions = {
                    IconButton(onClick = {
                        authViewModel.logout(authToken)
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = { BottomNavComponent(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val profile = uiState.profile) {
                is ProfileUIState.Loading -> LoadingUI()
                is ProfileUIState.Error -> Text(
                    "Gagal memuat profil: ${profile.message}",
                    color = MaterialTheme.colorScheme.error
                )
                is ProfileUIState.Success -> {
                    Text("Informasi Akun", style = MaterialTheme.typography.titleLarge)

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Lengkap") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            hasSubmittedProfile = true
                            libraryViewModel.putUserMe(authToken, name, username)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = name.isNotBlank() && username.isNotBlank()
                    ) { Text("Simpan Perubahan") }

                    Divider()
                    Text("Ganti Password", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password Lama") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Password Baru") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            hasSubmittedPassword = true
                            libraryViewModel.putUserMePassword(authToken, password, newPassword)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = password.isNotBlank() && newPassword.isNotBlank()
                    ) { Text("Ganti Password") }

                    Divider()

                    // Tombol logout di bawah juga
                    OutlinedButton(
                        onClick = { authViewModel.logout(authToken) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.ExitToApp, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Logout")
                    }
                }
            }
        }
    }
}