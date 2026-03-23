package org.delcom.pam_proyek1_ifs23049.network.data

data class ResponseMessage<T>(
    val status: String = "",
    val message: String = "",
    val data: T? = null
)