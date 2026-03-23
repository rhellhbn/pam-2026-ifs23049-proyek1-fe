package org.delcom.pam_proyek1_ifs23049.network.library.service

import okhttp3.MultipartBody
import org.delcom.pam_proyek1_ifs23049.network.data.ResponseMessage
import org.delcom.pam_proyek1_ifs23049.network.library.data.*
import retrofit2.http.*

interface LibraryApiService {

    @POST("auth/register")
    suspend fun postRegister(@Body request: RequestAuthRegister): ResponseMessage

    @POST("auth/login")
    suspend fun postLogin(@Body request: RequestAuthLogin): ResponseMessage

    @POST("auth/logout")
    suspend fun postLogout(@Body request: RequestAuthLogout): ResponseMessage

    @POST("auth/refresh-token")
    suspend fun postRefreshToken(@Body request: RequestAuthRefreshToken): ResponseMessage

    @GET("users/me")
    suspend fun getUserMe(@Header("Authorization") authToken: String): ResponseMessage

    @PUT("users/me")
    suspend fun putUserMe(
        @Header("Authorization") authToken: String,
        @Body request: RequestUserChange
    ): ResponseMessage

    @PUT("users/me/password")
    suspend fun putUserMePassword(
        @Header("Authorization") authToken: String,
        @Body request: RequestUserChangePassword
    ): ResponseMessage

    @Multipart
    @PUT("users/me/photo")
    suspend fun putUserMePhoto(
        @Header("Authorization") authToken: String,
        @Part file: MultipartBody.Part
    ): ResponseMessage

    @GET("books")
    suspend fun getBooks(
        @Header("Authorization") authToken: String,
        @Query("search") search: String? = null,
        @Query("page") page: Int? = null,
        @Query("perPage") perPage: Int? = null,
        @Query("genre") genre: String? = null,
        @Query("is_read") isRead: String? = null
    ): ResponseMessage

    @POST("books")
    suspend fun postBook(
        @Header("Authorization") authToken: String,
        @Body request: RequestBook
    ): ResponseMessage

    @GET("books/{id}")
    suspend fun getBookById(
        @Header("Authorization") authToken: String,
        @Path("id") bookId: String
    ): ResponseMessage

    @PUT("books/{id}")
    suspend fun putBook(
        @Header("Authorization") authToken: String,
        @Path("id") bookId: String,
        @Body request: RequestBook
    ): ResponseMessage

    @Multipart
    @PUT("books/{id}/cover")
    suspend fun putBookCover(
        @Header("Authorization") authToken: String,
        @Path("id") bookId: String,
        @Part file: MultipartBody.Part
    ): ResponseMessage

    @DELETE("books/{id}")
    suspend fun deleteBook(
        @Header("Authorization") authToken: String,
        @Path("id") bookId: String
    ): ResponseMessage
}