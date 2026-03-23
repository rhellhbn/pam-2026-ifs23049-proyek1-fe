package org.delcom.pam_proyek1_ifs23049.network.library.service

import okhttp3.MultipartBody
import org.delcom.pam_proyek1_ifs23049.network.data.ResponseMessage
import org.delcom.pam_proyek1_ifs23049.network.library.data.*
import retrofit2.http.*

interface LibraryApiService {

    // ── Auth ──────────────────────────────────────────────────────

    @POST("auth/register")
    suspend fun postRegister(
        @Body request: RequestAuthRegister
    ): ResponseMessage<ResponseAuthRegister?>

    @POST("auth/login")
    suspend fun postLogin(
        @Body request: RequestAuthLogin
    ): ResponseMessage<ResponseAuthLogin?>

    @POST("auth/logout")
    suspend fun postLogout(
        @Body request: RequestAuthLogout
    ): ResponseMessage<String?>

    @POST("auth/refresh-token")
    suspend fun postRefreshToken(
        @Body request: RequestAuthRefreshToken
    ): ResponseMessage<ResponseAuthLogin?>

    // ── User ──────────────────────────────────────────────────────

    @GET("users/me")
    suspend fun getUserMe(
        @Header("Authorization") authToken: String
    ): ResponseMessage<ResponseUser?>

    @PUT("users/me")
    suspend fun putUserMe(
        @Header("Authorization") authToken: String,
        @Body request: RequestUserChange
    ): ResponseMessage<String?>

    @PUT("users/me/password")
    suspend fun putUserMePassword(
        @Header("Authorization") authToken: String,
        @Body request: RequestUserChangePassword
    ): ResponseMessage<String?>

    @Multipart
    @PUT("users/me/photo")
    suspend fun putUserMePhoto(
        @Header("Authorization") authToken: String,
        @Part file: MultipartBody.Part
    ): ResponseMessage<String?>

    // ── Book ──────────────────────────────────────────────────────

    @GET("books")
    suspend fun getBooks(
        @Header("Authorization") authToken: String,
        @Query("search") search: String? = null,
        @Query("page") page: Int? = null,
        @Query("perPage") perPage: Int? = null,
        @Query("genre") genre: String? = null,
        @Query("is_read") isRead: String? = null
    ): ResponseMessage<ResponseBooks?>

    @POST("books")
    suspend fun postBook(
        @Header("Authorization") authToken: String,
        @Body request: RequestBook
    ): ResponseMessage<ResponseBookAdd?>

    @GET("books/{id}")
    suspend fun getBookById(
        @Header("Authorization") authToken: String,
        @Path("id") bookId: String
    ): ResponseMessage<ResponseBook?>

    @PUT("books/{id}")
    suspend fun putBook(
        @Header("Authorization") authToken: String,
        @Path("id") bookId: String,
        @Body request: RequestBook
    ): ResponseMessage<String?>

    @Multipart
    @PUT("books/{id}/cover")
    suspend fun putBookCover(
        @Header("Authorization") authToken: String,
        @Path("id") bookId: String,
        @Part file: MultipartBody.Part
    ): ResponseMessage<String?>

    @DELETE("books/{id}")
    suspend fun deleteBook(
        @Header("Authorization") authToken: String,
        @Path("id") bookId: String
    ): ResponseMessage<String?>
}