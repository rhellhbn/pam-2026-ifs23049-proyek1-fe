package org.delcom.pam_proyek1_ifs23049.network.library.service

import okhttp3.MultipartBody
import org.delcom.pam_proyek1_ifs23049.network.data.ResponseMessage
import org.delcom.pam_proyek1_ifs23049.network.library.data.*

interface ILibraryRepository {
    suspend fun postRegister(request: RequestAuthRegister): ResponseMessage
    suspend fun postLogin(request: RequestAuthLogin): ResponseMessage
    suspend fun postLogout(request: RequestAuthLogout): ResponseMessage
    suspend fun postRefreshToken(request: RequestAuthRefreshToken): ResponseMessage
    suspend fun getUserMe(authToken: String): ResponseMessage
    suspend fun putUserMe(authToken: String, request: RequestUserChange): ResponseMessage
    suspend fun putUserMePassword(authToken: String, request: RequestUserChangePassword): ResponseMessage
    suspend fun putUserMePhoto(authToken: String, file: MultipartBody.Part): ResponseMessage
    suspend fun getBooks(authToken: String, search: String?, page: Int?, perPage: Int?, genre: String?, isRead: String?): ResponseMessage
    suspend fun postBook(authToken: String, request: RequestBook): ResponseMessage
    suspend fun getBookById(authToken: String, bookId: String): ResponseMessage
    suspend fun putBook(authToken: String, bookId: String, request: RequestBook): ResponseMessage
    suspend fun putBookCover(authToken: String, bookId: String, file: MultipartBody.Part): ResponseMessage
    suspend fun deleteBook(authToken: String, bookId: String): ResponseMessage
}