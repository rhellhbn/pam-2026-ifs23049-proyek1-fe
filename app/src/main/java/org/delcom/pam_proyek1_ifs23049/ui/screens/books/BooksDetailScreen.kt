package org.delcom.pam_proyek1_ifs23049.ui.screens.books

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun BooksDetailScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    authViewModel: AuthViewModel,
    libraryViewModel: LibraryViewModel,
    bookId: String
) {
    val context = LocalContext.current
    val authState by authViewModel.uiState.collectAsState()
    val uiState by libraryViewModel.uiState.collectAsState()
    val authToken = (authState.auth as? AuthUIState.Success)?.data?.authToken ?: ""
    val coroutineScope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var hasDeleted by remember { mutableStateOf(false) }
    var hasChangedCover by remember { mutableStateOf(false) }

    val coverLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                try {
                    val stream = context.contentResolver.openInputStream(it) ?: return@launch
                    val bytes = stream.readBytes()
                    stream.close()
                    val part = MultipartBody.Part.createFormData(
                        "file", "cover.jpg",
                        bytes.toRequestBody("image/*".toMediaTypeOrNull())
                    )
                    hasChangedCover = true
                    libraryViewModel.putBookCover(authToken, bookId, part)
                } catch (e: Exception) {
                    coroutineScope.launch {
                        SuspendHelper.showSnackBar(snackbarHost, SuspendHelper.SnackBarType.ERROR, "Gagal: ${e.message}")
                    }
                }
            }
        }
    }

    LaunchedEffect(authToken, bookId) {
        if (authToken.isNotEmpty()) libraryViewModel.getBookById(authToken, bookId)
    }

    LaunchedEffect(uiState.bookDelete) {
        if (!hasDeleted) return@LaunchedEffect
        when (val state = uiState.bookDelete) {
            is BookActionUIState.Success -> {
                coroutineScope.launch { SuspendHelper.showSnackBar(snackbarHost, SuspendHelper.SnackBarType.SUCCESS, "Buku berhasil dihapus") }
                RouteHelper.back(navController)
            }
            is BookActionUIState.Error -> {
                coroutineScope.launch { SuspendHelper.showSnackBar(snackbarHost, SuspendHelper.SnackBarType.ERROR, state.message) }
            }
            else -> {}
        }
    }

    LaunchedEffect(uiState.bookChangeCover) {
        if (!hasChangedCover) return@LaunchedEffect
        when (val state = uiState.bookChangeCover) {
            is BookActionUIState.Success -> {
                coroutineScope.launch { SuspendHelper.showSnackBar(snackbarHost, SuspendHelper.SnackBarType.SUCCESS, "Cover berhasil diperbarui!") }
                libraryViewModel.getBookById(authToken, bookId)
                hasChangedCover = false
            }
            is BookActionUIState.Error -> {
                coroutineScope.launch { SuspendHelper.showSnackBar(snackbarHost, SuspendHelper.SnackBarType.ERROR, state.message) }
                hasChangedCover = false
            }
            else -> {}
        }
    }

    if (showDeleteDialog) {
        BottomDialog(onDismiss = { showDeleteDialog = false }, title = "Hapus Buku") {
            Text("Apakah kamu yakin ingin menghapus buku ini? Tindakan ini tidak dapat dibatalkan.",
                style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { hasDeleted = true; showDeleteDialog = false; libraryViewModel.deleteBook(authToken, bookId) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Icon(Icons.Default.Delete, null); Spacer(Modifier.width(8.dp)); Text("Hapus Buku") }
            OutlinedButton(onClick = { showDeleteDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("Batal") }
        }
    }

    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = "Detail Buku", showBack = true, onBack = { RouteHelper.back(navController) },
                actions = {
                    IconButton(onClick = { RouteHelper.to(navController, "books/$bookId/edit") }) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Hapus")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val book = uiState.book) {
            is BookUIState.Loading -> Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) { LoadingUI() }
            is BookUIState.Error -> Column(Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
                Text("Gagal memuat: ${book.message}", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                Button(onClick = { libraryViewModel.getBookById(authToken, bookId) }) { Text("Coba Lagi") }
            }
            is BookUIState.Success -> {
                val data = book.data
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues)
                        .verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cover Buku
                    Box(
                        modifier = Modifier.fillMaxWidth().height(220.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .clickable { coverLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!data.cover.isNullOrEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(data.cover).crossfade(true).build(),
                                contentDescription = "Cover Buku",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.MenuBook, null, modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Ketuk untuk menambah cover", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (hasChangedCover && uiState.bookChangeCover is BookActionUIState.Loading) {
                            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)) {
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        }
                    }

                    OutlinedButton(onClick = { coverLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Image, null); Spacer(Modifier.width(8.dp))
                        Text(if (data.cover.isNullOrEmpty()) "Tambah Cover" else "Ganti Cover")
                    }

                    HorizontalDivider()

                    Text(data.title, style = MaterialTheme.typography.headlineSmall)

                    Badge(
                        containerColor = if (data.isRead) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                        contentColor = if (data.isRead) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    ) {
                        Text(if (data.isRead) "✓ Sudah Dibaca" else "✗ Belum Dibaca",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }

                    HorizontalDivider()

                    InfoRow("Penulis", data.author)
                    if (!data.genre.isNullOrEmpty()) InfoRow("Genre", data.genre)
                    if (!data.isbn.isNullOrEmpty()) InfoRow("ISBN", data.isbn)
                    if (!data.publisher.isNullOrEmpty()) InfoRow("Penerbit", data.publisher)
                    if (data.year != null) InfoRow("Tahun", data.year.toString())

                    HorizontalDivider()

                    Text("Deskripsi", style = MaterialTheme.typography.titleMedium)
                    Text(data.description.ifEmpty { "Tidak ada deskripsi" }, style = MaterialTheme.typography.bodyMedium)

                    HorizontalDivider()

                    InfoRow("Ditambahkan", data.createdAt.take(10))
                    InfoRow("Diperbarui", data.updatedAt.take(10))

                    Spacer(Modifier.height(8.dp))

                    Button(onClick = { RouteHelper.to(navController, "books/$bookId/edit") }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Edit, null); Spacer(Modifier.width(8.dp)); Text("Edit Buku")
                    }
                    OutlinedButton(
                        onClick = { showDeleteDialog = true }, modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Icon(Icons.Default.Delete, null); Spacer(Modifier.width(8.dp)); Text("Hapus Buku") }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.4f))
        Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.6f))
    }
}