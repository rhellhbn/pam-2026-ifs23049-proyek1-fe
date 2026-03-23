package org.delcom.pam_proyek1_ifs23049.network.library.data

data class ResponseBooks(
    val books: List<ResponseBookData> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val perPage: Int = 10
)

data class ResponseBook(
    val book: ResponseBookData
)

data class ResponseBookData(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val author: String = "",
    val description: String = "",
    val genre: String = "Umum",
    val isbn: String? = null,
    val publisher: String? = null,
    val year: Int? = null,
    val isRead: Boolean = false,
    val cover: String? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
)

data class ResponseBookAdd(
    val bookId: String = ""
)