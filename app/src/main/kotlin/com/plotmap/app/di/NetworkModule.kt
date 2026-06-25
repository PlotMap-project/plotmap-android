package com.plotmap.app.di
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.plotmap.app.core.data.TokenManager
import com.plotmap.app.core.network.AuthInterceptor
import com.plotmap.app.core.network.PlotMapApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit

val networkModule =
    module {
        single {
            val json = Json { ignoreUnknownKeys = true }
            Retrofit.Builder()
                .baseUrl("https://thirty-mirrors-crash.loca.lt/api/v1/")
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .client(
                    OkHttpClient.Builder()
                        .addInterceptor(
                            AuthInterceptor(
                                tokenProvider = { get<TokenManager>().getToken() },
                            ),
                        )
                        .build(),
                )
                .build()
                .create(PlotMapApi::class.java)
        }
    }
