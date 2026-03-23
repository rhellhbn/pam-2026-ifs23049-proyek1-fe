package org.delcom.pam_proyek1_ifs23049.network.library.data

data class ResponseUser(
    val user: ResponseUserData
)

data class ResponseUserData(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val photo: String? = null,
    val bio: String? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
)