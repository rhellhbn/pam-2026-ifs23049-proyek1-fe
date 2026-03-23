package org.delcom.pam_proyek1_ifs23049.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.delcom.pam_proyek1_ifs23049.helper.*
import org.delcom.pam_proyek1_ifs23049.ui.components.*
import org.delcom.pam_proyek1_ifs23049.ui.viewmodels.*

@Composable
fun ProfileScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    libraryViewModel: LibraryViewModel
) {
    val context = LocalContext.current
    val authState by authViewModel.uiState.collectAsState()
    val uiState by libraryViewModel.uiState.collectAsState()
    val darkMode by authViewModel.darkMode.collectAsState()
    val systemDark = isSystemInDarkTheme()
    val isDark = darkMode ?: systemDark

    val authToken = (authState.auth as? AuthUIState.Success)?.data?.authToken ?: ""
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }
    var hasSubmittedProfile by remember { mutableStateOf(false) }
    var hasSubmittedPassword by remember { mutableStateOf(false) }
    var hasSubmittedPhoto by remember { mutableStateOf(false) }

    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                try {
                    val stream = context.contentResolver.openInputStream(it) ?: return@launch
                    val bytes = stream.readBytes()
                    stream.close()
                    val part = MultipartBody.Part.createFormData(
                        "file", "photo.jpg",
                        bytes.toRequestBody("image/*".toMediaTypeOrNull())
                    )
                    hasSubmittedPhoto = true
                    libraryViewModel.putUserMePhoto(authToken, part)
                } catch (e: Exception) {
                    SuspendHelper.showSnackBar(snackbarHostState, SuspendHelper.SnackBarType.ERROR, "Gagal: ${e.message}")
                }
            }
        }
    }

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

    LaunchedEffect(authState.auth) {
        if (authState.auth is AuthUIState.Error) {
            RouteHelper.to(navController, ConstHelper.RouteNames.AuthLogin.path, removeBackStack = true)
        }
    }

    LaunchedEffect(uiState.userChange) {
        if (!hasSubmittedProfile) return@LaunchedEffect
        when (val state = uiState.userChange) {
            is BookActionUIState.Success -> {
                coroutineScope.launch { SuspendHelper.showSnackBar(snackbarHostState, SuspendHelper.SnackBarType.SUCCESS, "Profil berhasil diperbarui!") }
                libraryViewModel.getProfile(authToken)
                hasSubmittedProfile = false
            }
            is BookActionUIState.Error -> {
                coroutineScope.launch { SuspendHelper.showSnackBar(snackbarHostState, SuspendHelper.SnackBarType.ERROR, state.message) }
                hasSubmittedProfile = false
            }
            else -> {}
        }
    }

    LaunchedEffect(uiState.userChangePassword) {
        if (!hasSubmittedPassword) return@LaunchedEffect
        when (val state = uiState.userChangePassword) {
            is BookActionUIState.Success -> {
                coroutineScope.launch { SuspendHelper.showSnackBar(snackbarHostState, SuspendHelper.SnackBarType.SUCCESS, "Password berhasil diubah!") }
                password = ""; newPassword = ""; hasSubmittedPassword = false
            }
            is BookActionUIState.Error -> {
                coroutineScope.launch { SuspendHelper.showSnackBar(snackbarHostState, SuspendHelper.SnackBarType.ERROR, state.message) }
                hasSubmittedPassword = false
            }
            else -> {}
        }
    }

    LaunchedEffect(uiState.userChangePhoto) {
        if (!hasSubmittedPhoto) return@LaunchedEffect
        when (val state = uiState.userChangePhoto) {
            is BookActionUIState.Success -> {
                coroutineScope.launch { SuspendHelper.showSnackBar(snackbarHostState, SuspendHelper.SnackBarType.SUCCESS, "Foto profil berhasil diperbarui!") }
                libraryViewModel.getProfile(authToken)
                hasSubmittedPhoto = false
            }
            is BookActionUIState.Error -> {
                coroutineScope.launch { SuspendHelper.showSnackBar(snackbarHostState, SuspendHelper.SnackBarType.ERROR, state.message) }
                hasSubmittedPhoto = false
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                CustomSnackbar(snackbarData = data, onDismiss = { snackbarHostState.currentSnackbarData?.dismiss() })
            }
        },
        topBar = {
            TopAppBarComponent(
                title = "Profil",
                actions = {
                    IconButton(onClick = { authViewModel.toggleDarkMode(!isDark) }) {
                        Icon(
                            if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDark) "Mode Terang" else "Mode Gelap"
                        )
                    }
                    IconButton(onClick = { authViewModel.logout(authToken) }) {
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
                is ProfileUIState.Loading -> Box(Modifier.fillMaxWidth(), Alignment.Center) { LoadingUI("Memuat profil...") }
                is ProfileUIState.Error -> Text("Gagal memuat profil: ${profile.message}", color = MaterialTheme.colorScheme.error)
                is ProfileUIState.Success -> {
                    val data = profile.data

                    // Foto Profil
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                .clickable { photoLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!data.photo.isNullOrEmpty()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context).data(data.photo).crossfade(true).build(),
                                    contentDescription = "Foto Profil",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, null, modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                            Box(
                                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Icon(Icons.Default.CameraAlt, null, tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(bottom = 6.dp).size(20.dp))
                            }
                        }
                        if (hasSubmittedPhoto && uiState.userChangePhoto is BookActionUIState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Text("Ketuk foto untuk mengganti", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(data.name, style = MaterialTheme.typography.titleLarge)
                        Text("@${data.username}", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    HorizontalDivider()

                    // Dark Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode, null)
                            Column {
                                Text("Mode Tampilan", style = MaterialTheme.typography.bodyLarge)
                                Text(if (isDark) "Mode Gelap aktif" else "Mode Terang aktif",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(checked = isDark, onCheckedChange = { authViewModel.toggleDarkMode(it) })
                    }

                    HorizontalDivider()

                    // Edit Info Akun
                    Text("Informasi Akun", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(value = name, onValueChange = { name = it },
                        label = { Text("Nama Lengkap") }, leadingIcon = { Icon(Icons.Default.Person, null) },
                        modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = username, onValueChange = { username = it },
                        label = { Text("Username") }, leadingIcon = { Icon(Icons.Default.AccountCircle, null) },
                        modifier = Modifier.fillMaxWidth())

                    val isSavingProfile = hasSubmittedProfile && uiState.userChange is BookActionUIState.Loading
                    Button(
                        onClick = { hasSubmittedProfile = true; libraryViewModel.putUserMe(authToken, name, username) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSavingProfile && name.isNotBlank() && username.isNotBlank()
                    ) {
                        if (isSavingProfile) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                        else { Icon(Icons.Default.Save, null); Spacer(Modifier.width(8.dp)); Text("Simpan Perubahan") }
                    }

                    HorizontalDivider()

                    // Ganti Password
                    Text("Ganti Password", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(value = password, onValueChange = { password = it },
                        label = { Text("Password Lama") }, leadingIcon = { Icon(Icons.Default.Lock, null) },
                        trailingIcon = { IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) } },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newPassword, onValueChange = { newPassword = it },
                        label = { Text("Password Baru") }, leadingIcon = { Icon(Icons.Default.LockOpen, null) },
                        trailingIcon = { IconButton(onClick = { showNewPassword = !showNewPassword }) {
                            Icon(if (showNewPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) } },
                        visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth())

                    val isSavingPassword = hasSubmittedPassword && uiState.userChangePassword is BookActionUIState.Loading
                    Button(
                        onClick = { hasSubmittedPassword = true; libraryViewModel.putUserMePassword(authToken, password, newPassword) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSavingPassword && password.isNotBlank() && newPassword.isNotBlank()
                    ) {
                        if (isSavingPassword) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                        else { Icon(Icons.Default.Key, null); Spacer(Modifier.width(8.dp)); Text("Ganti Password") }
                    }

                    HorizontalDivider()

                    OutlinedButton(
                        onClick = { authViewModel.logout(authToken) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.ExitToApp, null); Spacer(Modifier.width(8.dp)); Text("Logout")
                    }
                }
            }
        }
    }
}