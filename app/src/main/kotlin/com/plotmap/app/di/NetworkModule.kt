package com.plotmap.app.di
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.plotmap.app.core.data.TokenManager
import com.plotmap.app.core.network.AuthInterceptor
import com.plotmap.app.core.network.PlotMapApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

val networkModule =
    module {
        single {
            val json = Json { ignoreUnknownKeys = true }
            Retrofit.Builder()
                .baseUrl("https://harley-voice-enlargement-florida.trycloudflare.com/api/v1/")
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .client(
                    OkHttpClient.Builder()
                        .callTimeout(300, TimeUnit.SECONDS)
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(240, TimeUnit.SECONDS)
                        .writeTimeout(60, TimeUnit.SECONDS)
                        .addInterceptor(
                            AuthInterceptor(
                                tokenProvider = { get<TokenManager>().getToken() },
                            ),
                        )
                        .addInterceptor(
                            HttpLoggingInterceptor().apply {
                                level = HttpLoggingInterceptor.Level.BODY
                            },
                        )
                        .build(),
                )
                .build()
                .create(PlotMapApi::class.java)
        }
    }
