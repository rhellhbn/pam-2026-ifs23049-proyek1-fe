package org.delcom.pam_proyek1_ifs23049.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CustomSnackbar(
    snackbarData: SnackbarData,
    onDismiss: () -> Unit
) {
    val parts = snackbarData.visuals.message.split("|", limit = 2)
    val type = parts.getOrElse(0) { "info" }
    val message = parts.getOrElse(1) { snackbarData.visuals.message }

    val backgroundColor = when (type) {
        "success" -> Color(0xFF4CAF50)
        "error"   -> Color(0xFFE53935)
        "warning" -> Color(0xFFFFC107)
        else      -> Color(0xFF2196F3)
    }

    Snackbar(
        modifier = Modifier.padding(12.dp),
        containerColor = backgroundColor,
        contentColor = Color.White,
        action = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = Color.White)
            }
        }
    ) {
        Text(message)
    }
}