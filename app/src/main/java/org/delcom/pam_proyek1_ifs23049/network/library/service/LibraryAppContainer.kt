package org.delcom.pam_proyek1_ifs23049.network.library.service

import android.util.Log
import com.google.gson.*
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.delcom.pam_proyek1_ifs23049.network.data.ResponseMessage
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

class LibraryAppContainer : ILibraryAppContainer {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val rawResponseInterceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())
        val body = response.peekBody(Long.MAX_VALUE)
        Log.d("RAW_RESPONSE", "Code: ${response.code}")
        Log.d("RAW_RESPONSE", "Body: ${body.string()}")
        response
    }

    private val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    })

    private val sslContext = SSLContext.getInstance("SSL").apply {
        init(null, trustAllCerts, SecureRandom())
    }

    // Custom deserializer — baca "data" sebagai JsonElement mentah, tidak dipaksa ke tipe apapun
    private val responseMessageDeserializer = object : JsonDeserializer<ResponseMessage> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): ResponseMessage {
            val obj     = json.asJsonObject
            val status  = obj.get("status")?.asString  ?: ""
            val message = obj.get("message")?.asString ?: ""
            val data    = obj.get("data") // null kalau field tidak ada
            return ResponseMessage(status, message, data)
        }
    }

    private val gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(ResponseMessage::class.java, responseMessageDeserializer)
        .create()

    private val okHttpClient = OkHttpClient.Builder().apply {
        addInterceptor(rawResponseInterceptor)
        addInterceptor(loggingInterceptor)
        sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
        hostnameVerifier { _, _ -> true }
        connectTimeout(15, TimeUnit.SECONDS)
        readTimeout(30, TimeUnit.SECONDS)
        writeTimeout(30, TimeUnit.SECONDS)
    }.build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://pam-2026-proyek1-ifs23049-be.hellhbn.fun:8080/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(okHttpClient)
        .build()

    private val retrofitService: LibraryApiService by lazy {
        retrofit.create(LibraryApiService::class.java)
    }

    override val repository: ILibraryRepository by lazy {
        LibraryRepository(retrofitService)
    }
}