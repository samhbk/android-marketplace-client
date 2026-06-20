package com.marketplace.app.data.remote

import com.google.gson.Gson
import com.marketplace.app.data.local.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route

/**
 * Refreshes JWT using a plain OkHttp client (no interceptors / no circular dependency with Retrofit).
 */
class TokenAuthenticator(
    private val baseUrl: String,
    private val gson: Gson,
    private val tokenStore: TokenStore,
) : Authenticator {

    private val refreshClient = OkHttpClient()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.url.encodedPath.endsWith("/api/auth/refresh")) {
            return null
        }
        if (responseCount(response) > 2) {
            return null
        }

        val refresh = runBlocking { tokenStore.refreshToken() } ?: run {
            runBlocking { tokenStore.clear() }
            return null
        }

        val json = gson.toJson(RefreshRequest(refresh))
        val url = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val req = Request.Builder()
            .url("${url}api/auth/refresh")
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()

        val tokenResponse = try {
            refreshClient.newCall(req).execute()
        } catch (_: Exception) {
            runBlocking { tokenStore.clear() }
            return null
        }

        if (!tokenResponse.isSuccessful) {
            tokenResponse.close()
            runBlocking { tokenStore.clear() }
            return null
        }

        val bodyStr = tokenResponse.body?.string() ?: run {
            runBlocking { tokenStore.clear() }
            return null
        }

        val tokenBody = try {
            gson.fromJson(bodyStr, TokenResponse::class.java)
        } catch (_: Exception) {
            runBlocking { tokenStore.clear() }
            return null
        }

        runBlocking {
            tokenStore.save(tokenBody.token, tokenBody.refreshToken)
        }

        return response.request.newBuilder()
            .header("Authorization", "Bearer ${tokenBody.token}")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var n = 1
        var p = response.priorResponse
        while (p != null) {
            n++
            p = p.priorResponse
        }
        return n
    }
}
