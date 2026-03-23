package org.delcom.pam_proyek1_ifs23049.ui.screens.books

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
fun BooksDetailScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    authViewModel: AuthViewModel,
    libraryViewModel: LibraryViewModel,
    bookId: String
) {
    val authState by authViewModel.uiState.collectAsState()
    val uiState by libraryViewModel.uiState.collectAsState()
    val authToken = (authState.auth as? AuthUIState.Success)?.data?.authToken ?: ""
    val coroutineScope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var hasDeleted by remember { mutableStateOf(false) }

    LaunchedEffect(authToken, bookId) {
        if (authToken.isNotEmpty()) libraryViewModel.getBookById(authToken, bookId)
    }

    LaunchedEffect(uiState.bookDelete) {
        if (!hasDeleted) return@LaunchedEffect
        when (val state = uiState.bookDelete) {
            is BookActionUIState.Success -> {
                coroutineScope.launch {
                    SuspendHelper.showSnackBar(
                        snackbarHost,
                        SuspendHelper.SnackBarType.SUCCESS,
                        "Buku berhasil dihapus"
                    )
                }
                RouteHelper.back(navController)
            }
            is BookActionUIState.Error -> {
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

    // Bottom Dialog konfirmasi hapus
    if (showDeleteDialog) {
        BottomDialog(
            onDismiss = { showDeleteDialog = false },
            title = "Hapus Buku"
        ) {
            Text(
                text = "Apakah kamu yakin ingin menghapus buku ini? Tindakan ini tidak dapat dibatalkan.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    hasDeleted = true
                    showDeleteDialog = false
                    libraryViewModel.deleteBook(authToken, bookId)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Hapus Buku")
            }
            OutlinedButton(
                onClick = { showDeleteDialog = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Batal")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = "Detail Buku",
                showBack = true,
                onBack = { RouteHelper.back(navController) },
                actions = {
                    IconButton(onClick = {
                        RouteHelper.to(navController, "books/$bookId/edit")
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val book = uiState.book) {
            is BookUIState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingUI()
                }
            }
            is BookUIState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    Text("Gagal memuat: ${book.message}")
                }
            }
            is BookUIState.Success -> {
                val data = book.data
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Judul
                    Text(
                        text = data.title,
                        style = MaterialTheme.typography.headlineMedium
                    )

                    // Status badge — isAvailable diganti isRead
                    Badge(
                        containerColor = if (data.isRead)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer,
                        contentColor = if (data.isRead)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    ) {
                        Text(
                            text = if (data.isRead) "Sudah Dibaca" else "Belum Dibaca",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Divider()

                    // Info buku — category diganti genre, tambah publisher & year
                    InfoRow(label = "Penulis", value = data.author)
                    if (!data.genre.isNullOrEmpty()) InfoRow(label = "Genre", value = data.genre)
                    if (!data.isbn.isNullOrEmpty()) InfoRow(label = "ISBN", value = data.isbn)
                    if (!data.publisher.isNullOrEmpty()) InfoRow(label = "Penerbit", value = data.publisher)
                    if (data.year != null) InfoRow(label = "Tahun", value = data.year.toString())

                    Divider()

                    // Deskripsi
                    Text("Deskripsi", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = data.description.ifEmpty { "Tidak ada deskripsi" },
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Divider()

                    // Tanggal
                    InfoRow(label = "Ditambahkan", value = data.createdAt.take(10))
                    InfoRow(label = "Diperbarui", value = data.updatedAt.take(10))

                    Spacer(Modifier.height(16.dp))

                    // Tombol Edit
                    Button(
                        onClick = { RouteHelper.to(navController, "books/$bookId/edit") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Edit Buku")
                    }

                    // Tombol Hapus
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Hapus Buku")
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}