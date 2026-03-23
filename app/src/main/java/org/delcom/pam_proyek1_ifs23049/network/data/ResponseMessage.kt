package org.delcom.pam_proyek1_ifs23049.network.data

import com.google.gson.JsonElement

data class ResponseMessage(
    val status: String = "",
    val message: String = "",
    val data: JsonElement? = null
)