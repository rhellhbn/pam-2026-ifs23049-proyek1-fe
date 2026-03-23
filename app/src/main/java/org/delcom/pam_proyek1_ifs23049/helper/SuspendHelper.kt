package org.delcom.pam_proyek1_ifs23049.helper

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import com.google.gson.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.delcom.pam_proyek1_ifs23049.network.data.ResponseMessage
import retrofit2.HttpException
import java.lang.reflect.Type

object SuspendHelper {

    enum class SnackBarType(val title: String) {
        ERROR("error"),
        SUCCESS("success"),
        INFO("info"),
        WARNING("warning")
    }

    private val responseMessageDeserializer = object : JsonDeserializer<ResponseMessage> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): ResponseMessage {
            val obj     = json.asJsonObject
            val status  = obj.get("status")?.asString  ?: "error"
            val message = obj.get("message")?.asString ?: "Unknown error"
            val data    = obj.get("data")
            return ResponseMessage(status, message, data)
        }
    }

    private val gson = GsonBuilder()
        .registerTypeAdapter(ResponseMessage::class.java, responseMessageDeserializer)
        .create()

    suspend fun showSnackBar(
        snackbarHost: SnackbarHostState,
        type: SnackBarType,
        message: String
    ) {
        coroutineScope {
            launch {
                snackbarHost.showSnackbar(
                    message = "${type.title}|$message",
                    actionLabel = "Close",
                    duration = SnackbarDuration.Indefinite
                )
            }
            launch {
                delay(5_000)
                snackbarHost.currentSnackbarData?.dismiss()
            }
        }
    }

    suspend fun safeApiCall(
        apiCall: suspend () -> ResponseMessage
    ): ResponseMessage {
        return try {
            apiCall()
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val parsed = runCatching {
                gson.fromJson(errorBody, ResponseMessage::class.java)
            }.getOrNull()
            ResponseMessage(
                status  = "error",
                message = parsed?.message ?: "Server error ${e.code()}"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseMessage(
                status  = "error",
                message = e.message ?: "Unknown error"
            )
        }
    }
}