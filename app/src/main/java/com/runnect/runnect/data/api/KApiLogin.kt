package com.runnect.runnect.data.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.runnect.runnect.BuildConfig
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object KApiLogin {
    private var retrofit: Retrofit? = null

    private val json = Json {
        ignoreUnknownKeys = true // Field 값이 없는 경우 무시
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            val logger = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()
            retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.RUNNECT_BASE_URL)
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
        }
        return retrofit!!
    }

    inline fun <reified T> create(): T = getRetrofit().create<T>(T::class.java)

    object ServicePool {
        val loginService = create<KLoginService>() //다른 api들은 다 헤더에 토큰값을 넣어줘야 하는데 KApiSearch에는 없길래 여기에 만듦
    }
}