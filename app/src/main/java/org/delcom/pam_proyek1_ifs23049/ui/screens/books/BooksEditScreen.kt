package org.delcom.pam_proyek1_ifs23049.ui.screens.books

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.delcom.pam_proyek1_ifs23049.helper.*
import org.delcom.pam_proyek1_ifs23049.ui.components.TopAppBarComponent
import org.delcom.pam_proyek1_ifs23049.ui.viewmodels.*

@Composable
fun BooksEditScreen(
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

    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("Umum") }
    var isbn by remember { mutableStateOf("") }
    var publisher by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var isRead by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }
    var hasSubmitted by remember { mutableStateOf(false) }

    val isLoading = hasSubmitted && uiState.bookChange is BookActionUIState.Loading

    LaunchedEffect(authToken, bookId) {
        if (authToken.isNotEmpty()) libraryViewModel.getBookById(authToken, bookId)
    }

    // Isi form dengan data yang sudah ada
    LaunchedEffect(uiState.book) {
        if (!isInitialized && uiState.book is BookUIState.Success) {
            val data = (uiState.book as BookUIState.Success).data
            title = data.title
            author = data.author
            description = data.description
            genre = data.genre
            isbn = data.isbn ?: ""
            publisher = data.publisher ?: ""
            year = data.year?.toString() ?: ""
            isRead = data.isRead
            isInitialized = true
        }
    }

    LaunchedEffect(uiState.bookChange) {
        if (!hasSubmitted) return@LaunchedEffect
        when (val state = uiState.bookChange) {
            is BookActionUIState.Success -> {
                coroutineScope.launch {
                    SuspendHelper.showSnackBar(
                        snackbarHost,
                        SuspendHelper.SnackBarType.SUCCESS,
                        "Buku berhasil diperbarui!"
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

    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = "Edit Buku",
                showBack = true,
                onBack = { RouteHelper.back(navController) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("Judul Buku*") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = author, onValueChange = { author = it },
                label = { Text("Penulis*") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = genre, onValueChange = { genre = it },
                label = { Text("Genre") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = isbn, onValueChange = { isbn = it },
                label = { Text("ISBN") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = publisher, onValueChange = { publisher = it },
                label = { Text("Penerbit") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = year, onValueChange = { year = it },
                label = { Text("Tahun Terbit") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = description, onValueChange = { description = it },
                label = { Text("Deskripsi*") },
                modifier = Modifier.fillMaxWidth(), minLines = 4
            )

            // Status sudah dibaca
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Switch(checked = isRead, onCheckedChange = { isRead = it })
                Text(if (isRead) "Sudah Dibaca" else "Belum Dibaca")
            }

            Button(
                onClick = {
                    if (!isLoading) {
                        hasSubmitted = true
                        libraryViewModel.putBook(
                            authToken = authToken,
                            bookId = bookId,
                            title = title,
                            author = author,
                            description = description,
                            genre = genre.ifBlank { "Umum" },
                            isbn = isbn.ifBlank { null },
                            publisher = publisher.ifBlank { null },
                            year = year.toIntOrNull(),
                            isRead = isRead
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && title.isNotBlank() && author.isNotBlank() && description.isNotBlank()
            ) {
                if (isLoading) CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                else Text("Simpan Perubahan")
            }
        }
    }
}