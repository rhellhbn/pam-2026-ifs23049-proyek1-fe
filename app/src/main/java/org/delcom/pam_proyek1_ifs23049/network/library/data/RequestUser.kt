package org.delcom.pam_proyek1_ifs23049.network.library.data

data class RequestUserChange(
    val name: String,
    val username: String
)

data class RequestUserChangePassword(
    val password: String,
    val newPassword: String
)