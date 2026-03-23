package org.delcom.pam_proyek1_ifs23049

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.delcom.pam_proyek1_ifs23049.ui.UIApp
import org.delcom.pam_proyek1_ifs23049.ui.theme.LibraryTheme
import org.delcom.pam_proyek1_ifs23049.ui.viewmodels.AuthViewModel
import org.delcom.pam_proyek1_ifs23049.ui.viewmodels.LibraryViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val libraryViewModel: LibraryViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LibraryTheme {
                UIApp(
                    libraryViewModel = libraryViewModel,
                    authViewModel = authViewModel
                )
            }
        }
    }
}