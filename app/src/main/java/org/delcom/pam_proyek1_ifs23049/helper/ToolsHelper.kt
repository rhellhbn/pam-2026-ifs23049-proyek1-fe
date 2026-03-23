package org.delcom.pam_proyek1_ifs23049.helper

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

object ToolsHelper {

    private const val BASE_URL = "https://pam-2026-proyek1-ifs23049-be.hellhbn.fun:8080/"

    fun getBookImage(bookId: String, t: String = "0"): String {
        return "${BASE_URL}images/books/${bookId}?t=${t}"
    }

    fun getUserImage(userId: String, t: String = "0"): String {
        return "${BASE_URL}images/users/${userId}?t=${t}"
    }

    fun String.toRequestBodyText(): RequestBody {
        return this.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    fun uriToMultipart(
        context: Context,
        uri: Uri,
        partName: String
    ): MultipartBody.Part {
        val file = uriToFile(context, uri)
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }

    fun uriToFile(context: Context, uri: Uri): File {
        val file = File.createTempFile("upload", ".tmp", context.cacheDir)
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file
    }
}