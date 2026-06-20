package com.marketplace.app.data.repository

import com.google.gson.Gson
import com.marketplace.app.data.local.TokenStore
import com.marketplace.app.data.remote.LoginRequest
import com.marketplace.app.data.remote.MarketplaceApi
import com.marketplace.app.data.remote.TokenResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class MarketplaceRepositoryTest {

    private lateinit var api: MarketplaceApi
    private lateinit var tokenStore: TokenStore
    private lateinit var repository: MarketplaceRepository

    @Before
    fun setUp() {
        api = mockk()
        tokenStore = mockk(relaxed = true)
        repository = MarketplaceRepository(api, tokenStore, Gson())
    }

    @Test
    fun login_savesTokensOnSuccess() = runTest {
        val tokens = TokenResponse(token = "access", refreshToken = "refresh")
        coEvery { api.login(LoginRequest("a@b.com", "secret")) } returns Response.success(tokens)

        val result = repository.login("a@b.com", "secret")

        assertTrue(result.isSuccess)
        coVerify { tokenStore.save("access", "refresh") }
    }

    @Test
    fun login_returnsFailureMessageFromApi() = runTest {
        val body = """{"error":"Invalid credentials"}"""
        coEvery { api.login(any()) } returns Response.error(401, body.toResponseBody())

        val result = repository.login("a@b.com", "wrong")

        assertTrue(result.isFailure)
        assertEquals("Invalid credentials", result.exceptionOrNull()?.message)
    }
}
