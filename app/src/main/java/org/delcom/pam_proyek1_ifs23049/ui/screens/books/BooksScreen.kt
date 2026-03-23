package org.delcom.pam_proyek1_ifs23049.ui.screens.books

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import org.delcom.pam_proyek1_ifs23049.helper.*
import org.delcom.pam_proyek1_ifs23049.network.library.data.ResponseBookData
import org.delcom.pam_proyek1_ifs23049.ui.components.*
import org.delcom.pam_proyek1_ifs23049.ui.viewmodels.*

@Composable
fun BooksScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    libraryViewModel: LibraryViewModel
) {
    val authState by authViewModel.uiState.collectAsState()
    val uiState by libraryViewModel.uiState.collectAsState()
    val authToken = (authState.auth as? AuthUIState.Success)?.data?.authToken ?: ""

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Semua") }
    var currentPage by remember { mutableStateOf(1) }
    var allBooks by remember { mutableStateOf(listOf<ResponseBookData>()) }
    var hasMore by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()
    val lifecycleOwner = LocalLifecycleOwner.current

    val filters = listOf("Semua", "Sudah Dibaca", "Belum Dibaca")

    // ✅ Refresh saat screen kembali aktif (ON_RESUME)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && authToken.isNotEmpty()) {
                currentPage = 1
                allBooks = emptyList()
                hasMore = true
                val isRead = when (selectedFilter) {
                    "Sudah Dibaca" -> "true"
                    "Belum Dibaca" -> "false"
                    else -> null
                }
                libraryViewModel.getAllBooks(
                    authToken = authToken,
                    search = searchQuery.ifBlank { null },
                    page = 1,
                    perPage = 10,
                    isRead = isRead
                )
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // ✅ Refresh saat search atau filter berubah
    LaunchedEffect(searchQuery, selectedFilter) {
        if (authToken.isNotEmpty()) {
            currentPage = 1
            allBooks = emptyList()
            hasMore = true
            val isRead = when (selectedFilter) {
                "Sudah Dibaca" -> "true"
                "Belum Dibaca" -> "false"
                else -> null
            }
            libraryViewModel.getAllBooks(
                authToken = authToken,
                search = searchQuery.ifBlank { null },
                page = 1,
                perPage = 10,
                isRead = isRead
            )
        }
    }

    // Append data ke list
    LaunchedEffect(uiState.books) {
        when (val books = uiState.books) {
            is BooksUIState.Success -> {
                val newBooks = books.data
                allBooks = if (currentPage == 1) newBooks else allBooks + newBooks
                if (newBooks.size < 10) hasMore = false
            }
            else -> {}
        }
    }

    // Infinite scroll
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= allBooks.size - 3 &&
                    hasMore && uiState.books !is BooksUIState.Loading
                ) {
                    currentPage++
                    val isRead = when (selectedFilter) {
                        "Sudah Dibaca" -> "true"
                        "Belum Dibaca" -> "false"
                        else -> null
                    }
                    libraryViewModel.getAllBooks(
                        authToken = authToken,
                        search = searchQuery.ifBlank { null },
                        page = currentPage,
                        perPage = 10,
                        isRead = isRead
                    )
                }
            }
    }

    Scaffold(
        topBar = { TopAppBarComponent(title = "Daftar Buku") },
        bottomBar = { BottomNavComponent(navController) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                RouteHelper.to(navController, ConstHelper.RouteNames.BooksAdd.path)
            }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Buku")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari judul atau penulis...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                if (allBooks.isEmpty() && uiState.books is BooksUIState.Loading) {
                    item { LoadingUI() }
                } else if (allBooks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Tidak ada buku ditemukan")
                        }
                    }
                } else {
                    items(allBooks, key = { it.id }) { book ->
                        BookListItem(book = book, onClick = {
                            RouteHelper.to(navController, "books/${book.id}")
                        })
                    }
                    if (hasMore && uiState.books is BooksUIState.Loading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookListItem(book: ResponseBookData, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(book.title, style = MaterialTheme.typography.titleMedium)
                Text(book.author, style = MaterialTheme.typography.bodyMedium)
                if (!book.genre.isNullOrEmpty()) {
                    Text(book.genre, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.width(8.dp))
            Badge(
                containerColor = if (book.isRead)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.errorContainer
            ) {
                Text(if (book.isRead) "Dibaca" else "Belum")
            }
        }
    }
}