package org.delcom.pam_proyek1_ifs23049.network.library.data

data class ResponseAuthRegister(
    val userId: String = ""
)

data class ResponseAuthLogin(
    val authToken: String = "",
    val refreshToken: String = ""
)