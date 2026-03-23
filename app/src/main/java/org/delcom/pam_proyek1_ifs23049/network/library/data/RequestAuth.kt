package org.delcom.pam_proyek1_ifs23049.network.library.data

data class RequestAuthRegister(
    val name: String,
    val username: String,
    val password: String
)

data class RequestAuthLogin(
    val username: String,
    val password: String
)

data class RequestAuthLogout(
    val authToken: String
)

data class RequestAuthRefreshToken(
    val authToken: String,
    val refreshToken: String
)