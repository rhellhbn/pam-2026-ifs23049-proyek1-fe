package org.delcom.pam_proyek1_ifs23049.network.library.service

import okhttp3.MultipartBody
import org.delcom.pam_proyek1_ifs23049.network.data.ResponseMessage
import org.delcom.pam_proyek1_ifs23049.network.library.data.*

interface ILibraryRepository {

    // Auth
    suspend fun postRegister(request: RequestAuthRegister): ResponseMessage<ResponseAuthRegister?>
    suspend fun postLogin(request: RequestAuthLogin): ResponseMessage<ResponseAuthLogin?>
    suspend fun postLogout(request: RequestAuthLogout): ResponseMessage<String?>
    suspend fun postRefreshToken(request: RequestAuthRefreshToken): ResponseMessage<ResponseAuthLogin?>

    // User
    suspend fun getUserMe(authToken: String): ResponseMessage<ResponseUser?>
    suspend fun putUserMe(authToken: String, request: RequestUserChange): ResponseMessage<String?>
    suspend fun putUserMePassword(authToken: String, request: RequestUserChangePassword): ResponseMessage<String?>
    suspend fun putUserMePhoto(authToken: String, file: MultipartBody.Part): ResponseMessage<String?>

    // Book
    suspend fun getBooks(
        authToken: String,
        search: String? = null,
        page: Int? = null,
        perPage: Int? = null,
        genre: String? = null,
        isRead: String? = null
    ): ResponseMessage<ResponseBooks?>
    suspend fun postBook(authToken: String, request: RequestBook): ResponseMessage<ResponseBookAdd?>
    suspend fun getBookById(authToken: String, bookId: String): ResponseMessage<ResponseBook?>
    suspend fun putBook(authToken: String, bookId: String, request: RequestBook): ResponseMessage<String?>
    suspend fun putBookCover(authToken: String, bookId: String, file: MultipartBody.Part): ResponseMessage<String?>
    suspend fun deleteBook(authToken: String, bookId: String): ResponseMessage<String?>
}