package org.delcom.pam_proyek1_ifs23049.network.library.data

data class RequestBook(
    val title: String,
    val author: String,
    val description: String,
    val genre: String = "Umum",
    val isbn: String? = null,
    val publisher: String? = null,
    val year: Int? = null,
    val isRead: Boolean = false
)