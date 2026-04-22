package com.plotmap.app.core.network
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenProvider: () -> String?,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val request =
            original
                .newBuilder()
                .addHeader("Bypass-Tunnel-Reminder", "true")
                .addHeader("localtunnel-skip-warning", "true")

        val path = original.url.encodedPath
        val token = tokenProvider()?.trim()
        val isAuthRequest =
            path.endsWith("/auth/register") ||
                path.endsWith("/auth/login/email") ||
                path.endsWith("/auth/login/name")

        if (!isAuthRequest && !token.isNullOrBlank()) {
            request.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(request.build())
    }
}
