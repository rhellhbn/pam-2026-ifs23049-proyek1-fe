package org.delcom.pam_proyek1_ifs23049.ui.screens.books

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.delcom.pam_proyek1_ifs23049.helper.*
import org.delcom.pam_proyek1_ifs23049.ui.components.TopAppBarComponent
import org.delcom.pam_proyek1_ifs23049.ui.viewmodels.*

@Composable
fun BooksAddScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    authViewModel: AuthViewModel,
    libraryViewModel: LibraryViewModel
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
    var hasSubmitted by remember { mutableStateOf(false) }

    val isLoading = hasSubmitted && uiState.bookAdd is BookActionUIState.Loading

    LaunchedEffect(uiState.bookAdd) {
        if (!hasSubmitted) return@LaunchedEffect
        when (val state = uiState.bookAdd) {
            is BookActionUIState.Success -> {
                coroutineScope.launch {
                    SuspendHelper.showSnackBar(
                        snackbarHost,
                        SuspendHelper.SnackBarType.SUCCESS,
                        "Buku berhasil ditambahkan!"
                    )
                }
                libraryViewModel.resetBookAdd()
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
                hasSubmitted = false
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = "Tambah Buku",
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

            Button(
                onClick = {
                    if (!isLoading) {
                        hasSubmitted = true
                        libraryViewModel.postBook(
                            authToken = authToken,
                            title = title,
                            author = author,
                            description = description,
                            genre = genre.ifBlank { "Umum" },
                            isbn = isbn.ifBlank { null },
                            publisher = publisher.ifBlank { null },
                            year = year.toIntOrNull()
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
                else Text("Simpan Buku")
            }
        }
    }
}