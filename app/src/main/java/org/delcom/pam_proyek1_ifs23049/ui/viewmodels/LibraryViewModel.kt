package org.delcom.pam_proyek1_ifs23049.ui.viewmodels

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import org.delcom.pam_proyek1_ifs23049.network.library.data.*
import org.delcom.pam_proyek1_ifs23049.network.library.service.ILibraryRepository
import org.delcom.pam_proyek1_ifs23049.prefs.AuthTokenPref
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
    private val repository: ILibraryRepository,
    private val authTokenPref: AuthTokenPref
) : ViewModel() {

    private val gson = Gson()
    private val _uiState = MutableStateFlow(UIStateLibrary())
    val uiState = _uiState.asStateFlow()

    private fun JsonElement?.field(key: String): JsonElement? {
        if (this == null || !this.isJsonObject) return null
        val v = this.asJsonObject.get(key)
        return if (v == null || v.isJsonNull) null else v
    }

    private inline fun <reified T> JsonElement?.parse(): T? {
        if (this == null || this.isJsonNull) return null
        return try {
            gson.fromJson<T>(this, object : TypeToken<T>() {}.type)
        } catch (e: Exception) {
            android.util.Log.e("PARSE_ERROR", "Failed to parse ${T::class.java.simpleName}: ${e.message}")
            null
        }
    }

    fun resetBookAdd() {
        _uiState.update { it.copy(bookAdd = BookActionUIState.Loading) }
    }

    fun getProfile(authToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(profile = ProfileUIState.Loading) }
            val result = runCatching { repository.getUserMe(authToken) }.fold(
                onSuccess = { res ->
                    if (res.status == "success") {
                        val user = res.data.field("user").parse<ResponseUserData>()
                        if (user != null) ProfileUIState.Success(user)
                        else ProfileUIState.Error("Data user tidak ditemukan")
                    } else ProfileUIState.Error(res.message)
                },
                onFailure = { ProfileUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(profile = result) }
        }
    }

    fun putUserMe(authToken: String, name: String, username: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(userChange = BookActionUIState.Loading) }
            val result = runCatching {
                repository.putUserMe(authToken, RequestUserChange(name, username))
            }.fold(
                onSuccess = { if (it.status == "success") BookActionUIState.Success(it.message) else BookActionUIState.Error(it.message) },
                onFailure = { BookActionUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(userChange = result) }
        }
    }

    fun putUserMePassword(authToken: String, password: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(userChangePassword = BookActionUIState.Loading) }
            val result = runCatching {
                repository.putUserMePassword(authToken, RequestUserChangePassword(password, newPassword))
            }.fold(
                onSuccess = { if (it.status == "success") BookActionUIState.Success(it.message) else BookActionUIState.Error(it.message) },
                onFailure = { BookActionUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(userChangePassword = result) }
        }
    }

    fun putUserMePhoto(authToken: String, file: MultipartBody.Part) {
        viewModelScope.launch {
            _uiState.update { it.copy(userChangePhoto = BookActionUIState.Loading) }
            val result = runCatching { repository.putUserMePhoto(authToken, file) }.fold(
                onSuccess = { if (it.status == "success") BookActionUIState.Success(it.message) else BookActionUIState.Error(it.message) },
                onFailure = { BookActionUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(userChangePhoto = result) }
        }
    }

    fun getAllBooks(
        search: String? = null,
        page: Int? = null,
        perPage: Int? = 10,
        isRead: String? = null
    ) {
        viewModelScope.launch {
            val token = authTokenPref.getAuthToken()

            if (token.isNullOrEmpty()) {
                _uiState.update {
                    it.copy(books = BooksUIState.Error("Token kosong / belum login"))
                }
                return@launch
            }

            android.util.Log.d("TOKEN_DEBUG", "token = $token")

            _uiState.update { it.copy(books = BooksUIState.Loading) }

            val result = runCatching {
                repository.getBooks(token, search, page, perPage, null, isRead)
            }.fold(
                onSuccess = { res ->

                    // 🔥 DEBUG SEMUA RESPONSE
                    android.util.Log.d("BOOKS_STATUS", "status=${res.status}")
                    android.util.Log.d("BOOKS_MESSAGE", "message=${res.message}")
                    android.util.Log.d("BOOKS_DATA", "data=${res.data}")

                    if (res.status == "success") {
                        val books = res.data.parse<List<ResponseBookData>>() ?: emptyList()
                        BooksUIState.Success(books)
                    } else {
                        BooksUIState.Error(res.message)
                    }
                },
                onFailure = {
                    android.util.Log.e("BOOKS_ERROR", "error=${it.message}", it)
                    BooksUIState.Error(it.message ?: "Unknown error")
                }
            )

            _uiState.update { it.copy(books = result) }
        }
    }

    fun getBookById(authToken: String, bookId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(book = BookUIState.Loading) }
            val result = runCatching { repository.getBookById(authToken, bookId) }.fold(
                onSuccess = { res ->
                    if (res.status == "success") {
                        val book = res.data.field("book").parse<ResponseBookData>()
                        if (book != null) BookUIState.Success(book)
                        else BookUIState.Error("Data buku tidak ditemukan")
                    } else BookUIState.Error(res.message)
                },
                onFailure = { BookUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(book = result) }
        }
    }

    fun postBook(
        title: String, author: String, description: String,
        genre: String, isbn: String?, publisher: String?, year: Int?
    ) {
        viewModelScope.launch {
            val token = authTokenPref.getAuthToken()

            if (token.isNullOrEmpty()) {
                _uiState.update {
                    it.copy(bookAdd = BookActionUIState.Error("Token kosong"))
                }
                return@launch
            }

            _uiState.update { it.copy(bookAdd = BookActionUIState.Loading) }

            val result = runCatching {
                repository.postBook(
                    token,
                    RequestBook(title, author, description, genre, isbn, publisher, year)
                )
            }.fold(
                onSuccess = {
                    if (it.status == "success")
                        BookActionUIState.Success(it.message)
                    else
                        BookActionUIState.Error(it.message)
                },
                onFailure = {
                    BookActionUIState.Error(it.message ?: "Unknown error")
                }
            )

            _uiState.update { it.copy(bookAdd = result) }

            if (result is BookActionUIState.Success) {
                getAllBooks() // ✅ auto refresh
            }
        }
    }

    fun putBook(
        authToken: String, bookId: String,
        title: String, author: String, description: String,
        genre: String, isbn: String?, publisher: String?, year: Int?, isRead: Boolean
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(bookChange = BookActionUIState.Loading) }
            val result = runCatching {
                repository.putBook(authToken, bookId, RequestBook(title, author, description, genre, isbn, publisher, year, isRead))
            }.fold(
                onSuccess = { if (it.status == "success") BookActionUIState.Success(it.message) else BookActionUIState.Error(it.message) },
                onFailure = { BookActionUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(bookChange = result) }
        }
    }

    fun putBookCover(authToken: String, bookId: String, file: MultipartBody.Part) {
        viewModelScope.launch {
            _uiState.update { it.copy(bookChangeCover = BookActionUIState.Loading) }
            val result = runCatching { repository.putBookCover(authToken, bookId, file) }.fold(
                onSuccess = { if (it.status == "success") BookActionUIState.Success(it.message) else BookActionUIState.Error(it.message) },
                onFailure = { BookActionUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(bookChangeCover = result) }
        }
    }

    fun deleteBook(authToken: String, bookId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(bookDelete = BookActionUIState.Loading) }
            val result = runCatching { repository.deleteBook(authToken, bookId) }.fold(
                onSuccess = { if (it.status == "success") BookActionUIState.Success(it.message) else BookActionUIState.Error(it.message) },
                onFailure = { BookActionUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(bookDelete = result) }
        }
    }
}