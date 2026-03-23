package org.delcom.pam_proyek1_ifs23049.ui.viewmodels

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.delcom.pam_proyek1_ifs23049.network.library.data.*
import org.delcom.pam_proyek1_ifs23049.network.library.service.ILibraryRepository
import org.delcom.pam_proyek1_ifs23049.prefs.AuthTokenPref
import javax.inject.Inject

sealed interface AuthUIState {
    data class Success(val data: ResponseAuthLogin) : AuthUIState
    data class Error(val message: String) : AuthUIState
    object Loading : AuthUIState
}

sealed interface AuthActionUIState {
    data class Success(val message: String) : AuthActionUIState
    data class Error(val message: String) : AuthActionUIState
    object Loading : AuthActionUIState
}

sealed interface AuthLogoutUIState {
    data class Success(val message: String) : AuthLogoutUIState
    data class Error(val message: String) : AuthLogoutUIState
    object Loading : AuthLogoutUIState
}

data class UIStateAuth(
    val auth: AuthUIState = AuthUIState.Loading,
    val authRegister: AuthActionUIState = AuthActionUIState.Loading,
    val authLogout: AuthLogoutUIState = AuthLogoutUIState.Loading,
    val authRefreshToken: AuthActionUIState = AuthActionUIState.Loading,
)

@HiltViewModel
@Keep
class AuthViewModel @Inject constructor(
    private val repository: ILibraryRepository,
    private val authTokenPref: AuthTokenPref
) : ViewModel() {

    private val _uiState = MutableStateFlow(UIStateAuth())
    val uiState = _uiState.asStateFlow()

    private val _darkMode = MutableStateFlow<Boolean?>(null)
    val darkMode = _darkMode.asStateFlow()

    fun loadTokenFromPreferences() {
        viewModelScope.launch {
            _uiState.update { it.copy(auth = AuthUIState.Loading) }
            val authToken    = authTokenPref.getAuthToken()
            val refreshToken = authTokenPref.getRefreshToken()
            _darkMode.value  = authTokenPref.getDarkMode()
            _uiState.update {
                it.copy(
                    auth = if (authToken.isNullOrEmpty() || refreshToken.isNullOrEmpty())
                        AuthUIState.Error("Token tidak tersedia")
                    else
                        AuthUIState.Success(ResponseAuthLogin(authToken, refreshToken))
                )
            }
        }
    }

    fun toggleDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            authTokenPref.saveDarkMode(isDark)
            _darkMode.value = isDark
        }
    }

    fun register(name: String, username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(authRegister = AuthActionUIState.Loading) }
            val result = runCatching {
                repository.postRegister(RequestAuthRegister(name, username, password))
            }.fold(
                onSuccess = { if (it.status == "success") AuthActionUIState.Success(it.message) else AuthActionUIState.Error(it.message) },
                onFailure = { AuthActionUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(authRegister = result) }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(auth = AuthUIState.Loading) }
            val result = runCatching {
                repository.postLogin(RequestAuthLogin(username, password))
            }.fold(
                onSuccess = { res ->
                    val dataObj      = res.data?.takeIf { it.isJsonObject }?.asJsonObject
                    val authToken    = dataObj?.get("authToken")?.asString    ?: ""
                    val refreshToken = dataObj?.get("refreshToken")?.asString ?: ""
                    if (res.status == "success" && authToken.isNotEmpty()) {
                        authTokenPref.saveAuthToken(authToken)
                        authTokenPref.saveRefreshToken(refreshToken)
                        AuthUIState.Success(ResponseAuthLogin(authToken, refreshToken))
                    } else {
                        AuthUIState.Error(res.message.ifEmpty { "Login gagal" })
                    }
                },
                onFailure = { AuthUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(auth = result) }
        }
    }

    fun logout(authToken: String) {
        viewModelScope.launch {
            authTokenPref.clearAuthToken()
            authTokenPref.clearRefreshToken()
            _uiState.update { it.copy(auth = AuthUIState.Error("Logout"), authLogout = AuthLogoutUIState.Loading) }
            runCatching { repository.postLogout(RequestAuthLogout(authToken)) }
            _uiState.update { it.copy(authLogout = AuthLogoutUIState.Success("Berhasil logout")) }
        }
    }

    fun refreshToken(authToken: String, refreshToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(auth = AuthUIState.Loading, authRefreshToken = AuthActionUIState.Loading) }
            var newAuth: AuthUIState = AuthUIState.Loading
            var newRefresh: AuthActionUIState = AuthActionUIState.Loading
            runCatching {
                repository.postRefreshToken(RequestAuthRefreshToken(authToken, refreshToken))
            }.fold(
                onSuccess = { res ->
                    val dataObj      = res.data?.takeIf { it.isJsonObject }?.asJsonObject
                    val newAuthToken = dataObj?.get("authToken")?.asString    ?: ""
                    val newRefTok    = dataObj?.get("refreshToken")?.asString ?: ""
                    if (res.status == "success" && newAuthToken.isNotEmpty()) {
                        authTokenPref.saveAuthToken(newAuthToken)
                        authTokenPref.saveRefreshToken(newRefTok)
                        newAuth    = AuthUIState.Success(ResponseAuthLogin(newAuthToken, newRefTok))
                        newRefresh = AuthActionUIState.Success(res.message)
                    } else {
                        newAuth    = AuthUIState.Error(res.message)
                        newRefresh = AuthActionUIState.Error(res.message)
                    }
                },
                onFailure = {
                    newAuth    = AuthUIState.Error(it.message ?: "Unknown error")
                    newRefresh = AuthActionUIState.Error(it.message ?: "Unknown error")
                }
            )
            _uiState.update { it.copy(auth = newAuth, authRefreshToken = newRefresh) }
        }
    }
}