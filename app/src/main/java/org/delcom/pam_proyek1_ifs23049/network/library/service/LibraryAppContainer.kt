package org.delcom.pam_proyek1_ifs23049.network.library.service

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Interceptor
import okhttp3.Response
import android.util.Log
import org.delcom.pam_proyek1_ifs23049.network.data.ResponseMessage
import org.delcom.pam_proyek1_ifs23049.network.library.data.ResponseBooks
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class LibraryAppContainer : ILibraryAppContainer {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val rawResponseInterceptor = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val response = chain.proceed(request)
            val responseBody = response.peekBody(Long.MAX_VALUE)
            Log.d("RAW_RESPONSE", "Code: ${response.code}")
            Log.d("RAW_RESPONSE", "Body: ${responseBody.string()}")
            return response
        }
    }

    private val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    })

    private val sslContext = SSLContext.getInstance("SSL").apply {
        init(null, trustAllCerts, SecureRandom())
    }

    // Gson dengan config lenient agar tidak strict
    private val gson = GsonBuilder()
        .setLenient()
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