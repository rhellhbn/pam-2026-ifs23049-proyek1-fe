package org.delcom.pam_proyek1_ifs23049.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.delcom.pam_proyek1_ifs23049.helper.*
import org.delcom.pam_proyek1_ifs23049.ui.components.*
import org.delcom.pam_proyek1_ifs23049.ui.viewmodels.*

data class PanduanItem(
    val icon: ImageVector,
    val judul: String,
    val deskripsi: String
)

@Composable
fun HomeScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    libraryViewModel: LibraryViewModel
) {
    val authState by authViewModel.uiState.collectAsState()
    val uiState   by libraryViewModel.uiState.collectAsState()

    val authToken    = when (val auth = authState.auth) {
        is AuthUIState.Success -> auth.data.authToken
        else -> ""
    }
    val refreshToken = when (val auth = authState.auth) {
        is AuthUIState.Success -> auth.data.refreshToken
        else -> ""
    }

    LaunchedEffect(authState.authRefreshToken) {
        when (val state = authState.authRefreshToken) {
            is AuthActionUIState.Error -> {
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
        }
    }

    val panduanList = listOf(
        PanduanItem(
            icon = Icons.Default.MenuBook,
            judul = "Daftar Buku",
            deskripsi = "Tap menu \"Buku\" di bawah untuk melihat semua koleksi bukumu. Kamu bisa mencari buku berdasarkan judul atau penulis menggunakan kotak pencarian."
        ),
        PanduanItem(
            icon = Icons.Default.Add,
            judul = "Tambah Buku",
            deskripsi = "Di halaman Buku, tap tombol \"+\" di pojok kanan bawah untuk menambahkan buku baru. Isi judul, penulis, genre, dan deskripsi buku."
        ),
        PanduanItem(
            icon = Icons.Default.FilterList,
            judul = "Filter & Pencarian",
            deskripsi = "Gunakan chip filter di halaman Buku untuk menyaring buku berdasarkan status: Semua, Sudah Dibaca, atau Belum Dibaca."
        ),
        PanduanItem(
            icon = Icons.Default.Image,
            judul = "Cover Buku",
            deskripsi = "Buka detail buku lalu tap area cover atau tombol \"Tambah Cover\" untuk mengunggah foto cover buku dari galeri HP kamu."
        ),
        PanduanItem(
            icon = Icons.Default.Edit,
            judul = "Edit & Hapus Buku",
            deskripsi = "Di halaman detail buku, tap tombol \"Edit Buku\" untuk mengubah data buku, atau \"Hapus Buku\" untuk menghapusnya dari koleksi."
        ),
        PanduanItem(
            icon = Icons.Default.CheckCircle,
            judul = "Tandai Sudah Dibaca",
            deskripsi = "Saat mengedit buku, aktifkan toggle \"Sudah Dibaca\" untuk menandai buku yang telah selesai kamu baca."
        ),
        PanduanItem(
            icon = Icons.Default.Person,
            judul = "Profil & Foto",
            deskripsi = "Di halaman Profil, kamu bisa mengubah nama, username, dan password. Tap foto lingkaran untuk mengganti foto profil dari galeri."
        ),
        PanduanItem(
            icon = Icons.Default.DarkMode,
            judul = "Mode Gelap / Terang",
            deskripsi = "Di halaman Profil, gunakan toggle Mode Tampilan untuk beralih antara Mode Gelap dan Mode Terang sesuai selera."
        ),
        PanduanItem(
            icon = Icons.Default.ExitToApp,
            judul = "Logout",
            deskripsi = "Untuk keluar dari akun, buka halaman Profil dan tap tombol \"Logout\" di bagian bawah halaman."
        ),
    )

    Scaffold(
        topBar = { TopAppBarComponent(title = "Beranda") },
        bottomBar = { BottomNavComponent(navController) }
    ) { paddingValues ->

        if (authState.auth is AuthUIState.Loading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { LoadingUI("Memeriksa sesi...") }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {

            // ── Greeting ──────────────────────────────────────────────────
            item {
                when (val profile = uiState.profile) {
                    is ProfileUIState.Success -> {
                        Text(
                            text = "Halo, ${profile.data.name}! 👋",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Selamat datang di Aplikasi Perpustakaan",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {
                        Text(
                            text = "Halo! 👋",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Selamat datang di Aplikasi Perpustakaan",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Banner navigasi cepat ──────────────────────────────────────
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = "Panduan Penggunaan",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Pelajari cara menggunakan fitur aplikasi di bawah ini.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // ── Judul seksi ───────────────────────────────────────────────
            item {
                Text(
                    text = "Cara Menggunakan Aplikasi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // ── Daftar panduan ────────────────────────────────────────────
            items(panduanList) { panduan ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .padding(top = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = panduan.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = panduan.judul,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = panduan.deskripsi,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ── Footer ────────────────────────────────────────────────────
            item {
                Spacer(Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Aplikasi Perpustakaan — IFS23049",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}