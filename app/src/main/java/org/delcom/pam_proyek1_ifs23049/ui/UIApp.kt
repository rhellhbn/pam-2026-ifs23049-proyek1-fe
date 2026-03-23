package org.delcom.pam_proyek1_ifs23049.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.*
import org.delcom.pam_proyek1_ifs23049.helper.ConstHelper
import org.delcom.pam_proyek1_ifs23049.ui.components.CustomSnackbar
import org.delcom.pam_proyek1_ifs23049.ui.screens.*
import org.delcom.pam_proyek1_ifs23049.ui.screens.auth.*
import org.delcom.pam_proyek1_ifs23049.ui.screens.books.*
import org.delcom.pam_proyek1_ifs23049.ui.viewmodels.AuthUIState
import org.delcom.pam_proyek1_ifs23049.ui.viewmodels.AuthViewModel
import org.delcom.pam_proyek1_ifs23049.ui.viewmodels.LibraryViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UIApp(
    navController: NavHostController = rememberNavController(),
    libraryViewModel: LibraryViewModel,
    authViewModel: AuthViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val authState by authViewModel.uiState.collectAsState()

    // Cek token saat app pertama dibuka
    LaunchedEffect(Unit) {
        authViewModel.loadTokenFromPreferences()
    }

    // Selama cek token, tampilkan loading di tengah layar
    if (authState.auth is AuthUIState.Loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Setelah cek token selesai, tentukan halaman awal
    val startDestination = if (authState.auth is AuthUIState.Success) {
        ConstHelper.RouteNames.Home.path
    } else {
        ConstHelper.RouteNames.AuthLogin.path
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                CustomSnackbar(
                    snackbarData = data,
                    onDismiss = { snackbarHostState.currentSnackbarData?.dismiss() }
                )
            }
        }
    ) { _ ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(ConstHelper.RouteNames.AuthLogin.path) {
                AuthLoginScreen(navController, snackbarHostState, authViewModel)
            }
            composable(ConstHelper.RouteNames.AuthRegister.path) {
                AuthRegisterScreen(navController, snackbarHostState, authViewModel)
            }
            composable(ConstHelper.RouteNames.Home.path) {
                HomeScreen(navController, authViewModel, libraryViewModel)
            }
            composable(ConstHelper.RouteNames.Profile.path) {
                ProfileScreen(navController, authViewModel, libraryViewModel)
            }
            composable(ConstHelper.RouteNames.Books.path) {
                BooksScreen(navController, authViewModel, libraryViewModel)
            }
            composable(ConstHelper.RouteNames.BooksAdd.path) {
                BooksAddScreen(navController, snackbarHostState, authViewModel, libraryViewModel)
            }
            composable(
                route = ConstHelper.RouteNames.BooksDetail.path,
                arguments = listOf(navArgument("bookId") { type = NavType.StringType })
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                BooksDetailScreen(navController, snackbarHostState, authViewModel, libraryViewModel, bookId)
            }
            composable(
                route = ConstHelper.RouteNames.BooksEdit.path,
                arguments = listOf(navArgument("bookId") { type = NavType.StringType })
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                BooksEditScreen(navController, snackbarHostState, authViewModel, libraryViewModel, bookId)
            }
        }
    }
}