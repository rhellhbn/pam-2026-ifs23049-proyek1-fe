package org.delcom.pam_proyek1_ifs23049.ui.viewmodels

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import org.delcom.pam_proyek1_ifs23049.network.library.data.*
import org.delcom.pam_proyek1_ifs23049.network.library.service.ILibraryRepository
import javax.inject.Inject

sealed interface ProfileUIState {
    data class Success(val data: ResponseUserData) : ProfileUIState
    data class Error(val message: String) : ProfileUIState
    object Loading : ProfileUIState
}

sealed interface BooksUIState {
    data class Success(val data: List<ResponseBookData>) : BooksUIState
    data class Error(val message: String) : BooksUIState
    object Loading : BooksUIState
}

sealed interface BookUIState {
    data class Success(val data: ResponseBookData) : BookUIState
    data class Error(val message: String) : BookUIState
    object Loading : BookUIState
}

sealed interface BookActionUIState {
    data class Success(val message: String) : BookActionUIState
    data class Error(val message: String) : BookActionUIState
    object Loading : BookActionUIState
}

data class UIStateLibrary(
    val profile: ProfileUIState = ProfileUIState.Loading,
    val books: BooksUIState = BooksUIState.Loading,
    val book: BookUIState = BookUIState.Loading,
    val bookAdd: BookActionUIState = BookActionUIState.Loading,
    val bookChange: BookActionUIState = BookActionUIState.Loading,
    val bookDelete: BookActionUIState = BookActionUIState.Loading,
    val bookChangeCover: BookActionUIState = BookActionUIState.Loading,
    val userChange: BookActionUIState = BookActionUIState.Loading,
    val userChangePassword: BookActionUIState = BookActionUIState.Loading,
    val userChangePhoto: BookActionUIState = BookActionUIState.Loading,
)

@HiltViewModel
@Keep
class LibraryViewModel @Inject constructor(
    private val repository: ILibraryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UIStateLibrary())
    val uiState = _uiState.asStateFlow()

    fun getProfile(authToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(profile = ProfileUIState.Loading) }
            _uiState.update { state ->
                val result = runCatching {
                    repository.getUserMe(authToken)
                }.fold(
                    onSuccess = {
                        if (it.status == "success" && it.data != null)
                            ProfileUIState.Success(it.data.user)
                        else ProfileUIState.Error(it.message)
                    },
                    onFailure = { ProfileUIState.Error(it.message ?: "Unknown error") }
                )
                state.copy(profile = result)
            }
        }
    }

    fun putUserMe(authToken: String, name: String, username: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(userChange = BookActionUIState.Loading) }
            _uiState.update { state ->
                val result = runCatching {
                    repository.putUserMe(authToken, RequestUserChange(name, username))
                }.fold(
                    onSuccess = {
                        if (it.status == "success") BookActionUIState.Success(it.message)
                        else BookActionUIState.Error(it.message)
                    },
                    onFailure = { BookActionUIState.Error(it.message ?: "Unknown error") }
                )
                state.copy(userChange = result)
            }
        }
    }

    fun putUserMePassword(authToken: String, password: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(userChangePassword = BookActionUIState.Loading) }
            _uiState.update { state ->
                val result = runCatching {
                    repository.putUserMePassword(
                        authToken,
                        RequestUserChangePassword(password, newPassword)
                    )
                }.fold(
                    onSuccess = {
                        if (it.status == "success") BookActionUIState.Success(it.message)
                        else BookActionUIState.Error(it.message)
                    },
                    onFailure = { BookActionUIState.Error(it.message ?: "Unknown error") }
                )
                state.copy(userChangePassword = result)
            }
        }
    }

    fun putUserMePhoto(authToken: String, file: MultipartBody.Part) {
        viewModelScope.launch {
            _uiState.update { it.copy(userChangePhoto = BookActionUIState.Loading) }
            _uiState.update { state ->
                val result = runCatching {
                    repository.putUserMePhoto(authToken, file)
                }.fold(
                    onSuccess = {
                        if (it.status == "success") BookActionUIState.Success(it.message)
                        else BookActionUIState.Error(it.message)
                    },
                    onFailure = { BookActionUIState.Error(it.message ?: "Unknown error") }
                )
                state.copy(userChangePhoto = result)
            }
        }
    }

    fun getAllBooks(
        authToken: String,
        search: String? = null,
        page: Int? = null,
        perPage: Int? = 10,
        isRead: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(books = BooksUIState.Loading) }
            _uiState.update { state ->
                val result = runCatching {
                    repository.getBooks(authToken, search, page, perPage, null, isRead)
                }.fold(
                    onSuccess = {
                        android.util.Log.d("BOOKS_DEBUG", "status: ${it.status}")
                        android.util.Log.d("BOOKS_DEBUG", "message: ${it.message}")
                        android.util.Log.d("BOOKS_DEBUG", "data: ${it.data}")
                        android.util.Log.d("BOOKS_DEBUG", "data class: ${it.data?.javaClass?.name}")
                        if (it.status == "success" && it.data != null)
                            BooksUIState.Success(it.data.books)
                        else BooksUIState.Error(it.message)
                    },
                    onFailure = {
                        android.util.Log.e("BOOKS_DEBUG", "error: ${it.message}", it)
                        BooksUIState.Error(it.message ?: "Unknown error")
                    }
                )
                state.copy(books = result)
            }
        }
    }

    fun getBookById(authToken: String, bookId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(book = BookUIState.Loading) }
            _uiState.update { state ->
                val result = runCatching {
                    repository.getBookById(authToken, bookId)
                }.fold(
                    onSuccess = {
                        if (it.status == "success" && it.data != null)
                            BookUIState.Success(it.data.book)
                        else BookUIState.Error(it.message)
                    },
                    onFailure = { BookUIState.Error(it.message ?: "Unknown error") }
                )
                state.copy(book = result)
            }
        }
    }

    fun postBook(
        authToken: String,
        title: String,
        author: String,
        description: String,
        genre: String,
        isbn: String?,
        publisher: String?,
        year: Int?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(bookAdd = BookActionUIState.Loading) }
            _uiState.update { state ->
                val result = runCatching {
                    repository.postBook(
                        authToken,
                        RequestBook(
                            title = title,
                            author = author,
                            description = description,
                            genre = genre,
                            isbn = isbn,
                            publisher = publisher,
                            year = year
                        )
                    )
                }.fold(
                    onSuccess = {
                        if (it.status == "success") BookActionUIState.Success(it.message)
                        else BookActionUIState.Error(it.message)
                    },
                    onFailure = { BookActionUIState.Error(it.message ?: "Unknown error") }
                )
                state.copy(bookAdd = result)
            }
        }
    }

    fun putBook(
        authToken: String,
        bookId: String,
        title: String,
        author: String,
        description: String,
        genre: String,
        isbn: String?,
        publisher: String?,
        year: Int?,
        isRead: Boolean
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(bookChange = BookActionUIState.Loading) }
            _uiState.update { state ->
                val result = runCatching {
                    repository.putBook(
                        authToken, bookId,
                        RequestBook(
                            title = title,
                            author = author,
                            description = description,
                            genre = genre,
                            isbn = isbn,
                            publisher = publisher,
                            year = year,
                            isRead = isRead
                        )
                    )
                }.fold(
                    onSuccess = {
                        if (it.status == "success") BookActionUIState.Success(it.message)
                        else BookActionUIState.Error(it.message)
                    },
                    onFailure = { BookActionUIState.Error(it.message ?: "Unknown error") }
                )
                state.copy(bookChange = result)
            }
        }
    }

    fun putBookCover(authToken: String, bookId: String, file: MultipartBody.Part) {
        viewModelScope.launch {
            _uiState.update { it.copy(bookChangeCover = BookActionUIState.Loading) }
            _uiState.update { state ->
                val result = runCatching {
                    repository.putBookCover(authToken, bookId, file)
                }.fold(
                    onSuccess = {
                        if (it.status == "success") BookActionUIState.Success(it.message)
                        else BookActionUIState.Error(it.message)
                    },
                    onFailure = { BookActionUIState.Error(it.message ?: "Unknown error") }
                )
                state.copy(bookChangeCover = result)
            }
        }
    }

    fun deleteBook(authToken: String, bookId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(bookDelete = BookActionUIState.Loading) }
            _uiState.update { state ->
                val result = runCatching {
                    repository.deleteBook(authToken, bookId)
                }.fold(
                    onSuccess = {
                        if (it.status == "success") BookActionUIState.Success(it.message)
                        else BookActionUIState.Error(it.message)
                    },
                    onFailure = { BookActionUIState.Error(it.message ?: "Unknown error") }
                )
                state.copy(bookDelete = result)
            }
        }
    }
}