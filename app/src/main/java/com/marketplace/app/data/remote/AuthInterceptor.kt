package com.marketplace.app.data.remote

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenStore: com.marketplace.app.data.local.TokenStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val path = chain.request().url.encodedPath
        if (path.endsWith("/api/auth/login") ||
            path.endsWith("/api/auth/register") ||
            path.endsWith("/api/auth/refresh")
        ) {
            return chain.proceed(chain.request())
        }

        val token = runBlocking { tokenStore.accessToken() }
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }
}
