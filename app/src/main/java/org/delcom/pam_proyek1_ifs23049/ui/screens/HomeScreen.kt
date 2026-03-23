package org.delcom.pam_proyek1_ifs23049.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.delcom.pam_proyek1_ifs23049.helper.*
import org.delcom.pam_proyek1_ifs23049.ui.components.*
import org.delcom.pam_proyek1_ifs23049.ui.viewmodels.*

@Composable
fun HomeScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    libraryViewModel: LibraryViewModel
) {
    val authState by authViewModel.uiState.collectAsState()
    val uiState by libraryViewModel.uiState.collectAsState()

    val authToken = when (val auth = authState.auth) {
        is AuthUIState.Success -> auth.data.authToken
        else -> ""
    }

    val refreshToken = when (val auth = authState.auth) {
        is AuthUIState.Success -> auth.data.refreshToken
        else -> ""
    }

    // Cek apakah data gagal karena token expired
    LaunchedEffect(uiState.profile) {
        if (uiState.profile is ProfileUIState.Error) {
            val msg = (uiState.profile as ProfileUIState.Error).message
            if (msg.contains("tidak valid", ignoreCase = true) ||
                msg.contains("unauthorized", ignoreCase = true) ||
                msg.contains("expired", ignoreCase = true)
            ) {
                // Token expired — coba refresh
                if (refreshToken.isNotEmpty()) {
                    authViewModel.refreshToken(authToken, refreshToken)
                } else {
                    // Tidak ada refresh token — paksa logout ke login
                    RouteHelper.to(
                        navController,
                        ConstHelper.RouteNames.AuthLogin.path,
                        removeBackStack = true
                    )
                }
            }
        }
    }

    // Setelah refresh token berhasil, load ulang data
    LaunchedEffect(authState.authRefreshToken) {
        when (val state = authState.authRefreshToken) {
            is AuthActionUIState.Success -> {
                val newToken = (authState.auth as? AuthUIState.Success)?.data?.authToken ?: ""
                if (newToken.isNotEmpty()) {
                    libraryViewModel.getProfile(newToken)
                    libraryViewModel.getAllBooks(newToken)
                }
            }
            is AuthActionUIState.Error -> {
                // Refresh token juga gagal — paksa login ulang
                RouteHelper.to(
                    navController,
                    ConstHelper.RouteNames.AuthLogin.path,
                    removeBackStack = true
                )
            }
            else -> {}
        }
    }

    LaunchedEffect(authToken) {
        if (authToken.isNotEmpty()) {
            libraryViewModel.getProfile(authToken)
            libraryViewModel.getAllBooks(authToken)
        }
    }

    Scaffold(
        topBar = { TopAppBarComponent(title = "Beranda") },
        bottomBar = { BottomNavComponent(navController) }
    ) { paddingValues ->

        if (authState.auth is AuthUIState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                LoadingUI("Memeriksa sesi...")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val profile = uiState.profile) {
                is ProfileUIState.Success -> {
                    Text(
                        text = "Halo, ${profile.data.name}!",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Selamat datang di Perpustakaan",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> {
                    Text(
                        text = "Halo!",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            Text(
                text = "Statistik Buku",
                style = MaterialTheme.typography.titleMedium
            )

            when (val books = uiState.books) {
                is BooksUIState.Success -> {
                    val total = books.data.size
                    val sudahDibaca = books.data.count { it.isRead }
                    val belumDibaca = total - sudahDibaca
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusCard("Total", total.toString(), Modifier.weight(1f))
                        StatusCard("Dibaca", sudahDibaca.toString(), Modifier.weight(1f))
                        StatusCard("Belum", belumDibaca.toString(), Modifier.weight(1f))
                    }
                }
                is BooksUIState.Loading -> LoadingUI()
                is BooksUIState.Error -> {
                    val msg = books.message
                    if (!msg.contains("tidak valid", ignoreCase = true)) {
                        Text(
                            text = "Gagal memuat data: $msg",
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        LoadingUI("Memperbarui sesi...")
                    }
                }
            }
        }
    }
}