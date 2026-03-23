package org.delcom.pam_proyek1_ifs23049.network.library.service

import okhttp3.MultipartBody
import org.delcom.pam_proyek1_ifs23049.helper.SuspendHelper
import org.delcom.pam_proyek1_ifs23049.network.data.ResponseMessage
import org.delcom.pam_proyek1_ifs23049.network.library.data.*

class LibraryRepository(
    private val apiService: LibraryApiService
) : ILibraryRepository {

    override suspend fun postRegister(request: RequestAuthRegister): ResponseMessage =
        SuspendHelper.safeApiCall { apiService.postRegister(request) }

    override suspend fun postLogin(request: RequestAuthLogin): ResponseMessage =
        SuspendHelper.safeApiCall { apiService.postLogin(request) }

    override suspend fun postLogout(request: RequestAuthLogout): ResponseMessage =
        SuspendHelper.safeApiCall { apiService.postLogout(request) }

    override suspend fun postRefreshToken(request: RequestAuthRefreshToken): ResponseMessage =
        SuspendHelper.safeApiCall { apiService.postRefreshToken(request) }

    override suspend fun getUserMe(authToken: String): ResponseMessage =
        SuspendHelper.safeApiCall { apiService.getUserMe("Bearer $authToken") }

    override suspend fun putUserMe(authToken: String, request: RequestUserChange): ResponseMessage =
        SuspendHelper.safeApiCall { apiService.putUserMe("Bearer $authToken", request) }

    override suspend fun putUserMePassword(authToken: String, request: RequestUserChangePassword): ResponseMessage =
        SuspendHelper.safeApiCall { apiService.putUserMePassword("Bearer $authToken", request) }

    override suspend fun putUserMePhoto(authToken: String, file: MultipartBody.Part): ResponseMessage =
        SuspendHelper.safeApiCall { apiService.putUserMePhoto("Bearer $authToken", file) }

    override suspend fun getBooks(authToken: String, search: String?, page: Int?, perPage: Int?, genre: String?, isRead: String?): ResponseMessage =
        SuspendHelper.safeApiCall { apiService.getBooks("Bearer $authToken", search, page, perPage, genre, isRead) }

    override suspend fun postBook(authToken: String, request: RequestBook): ResponseMessage =
        SuspendHelper.safeApiCall { apiService.postBook("Bearer $authToken", request) }

    override suspend fun getBookById(authToken: String, bookId: String): ResponseMessage =
        SuspendHelper.safeApiCall { apiService.getBookById("Bearer $authToken", bookId) }

    override suspend fun putBook(authToken: String, bookId: String, request: RequestBook): ResponseMessage =
        SuspendHelper.safeApiCall { apiService.putBook("Bearer $authToken", bookId, request) }

    override suspend fun putBookCover(authToken: String, bookId: String, file: MultipartBody.Part): ResponseMessage =
        SuspendHelper.safeApiCall { apiService.putBookCover("Bearer $authToken", bookId, file) }

    override suspend fun deleteBook(authToken: String, bookId: String): ResponseMessage =
        SuspendHelper.safeApiCall { apiService.deleteBook("Bearer $authToken", bookId) }
}