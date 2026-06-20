package com.marketplace.app.data.repository

import com.google.gson.Gson
import com.marketplace.app.data.local.TokenStore
import com.marketplace.app.data.remote.ApiError
import com.marketplace.app.data.remote.CategoryDto
import com.marketplace.app.data.remote.CreateOrderRequest
import com.marketplace.app.data.remote.LoginRequest
import com.marketplace.app.data.remote.LogoutRequest
import com.marketplace.app.data.remote.MarketplaceApi
import com.marketplace.app.data.remote.OrderDto
import com.marketplace.app.data.remote.OrderLineRequest
import com.marketplace.app.data.remote.ProductDto
import com.marketplace.app.data.remote.ProductPageDto
import com.marketplace.app.data.remote.RegisterRequest
import com.marketplace.app.data.remote.UserProfileDto
import com.marketplace.app.data.remote.WishlistResponse
import retrofit2.Response

class MarketplaceRepository(
    private val api: MarketplaceApi,
    private val tokenStore: TokenStore,
    private val gson: Gson,
) {

    private fun <T> Response<T>.failureMessage(): String {
        val err = errorBody()?.string()?.let {
            runCatching { gson.fromJson(it, ApiError::class.java).error }.getOrNull()
        }
        return err ?: message().ifBlank { "Request failed (${code()})" }
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        val r = api.login(LoginRequest(email, password))
        if (!r.isSuccessful) return Result.failure(Exception(r.failureMessage()))
        val body = r.body() ?: return Result.failure(Exception("Empty response"))
        tokenStore.save(body.token, body.refreshToken)
        return Result.success(Unit)
    }

    suspend fun register(
        email: String,
        password: String,
        displayName: String,
        asSeller: Boolean,
        phone: String?,
        storeName: String?,
    ): Result<Unit> {
        val r = api.register(
            RegisterRequest(
                email = email,
                password = password,
                displayName = displayName,
                role = if (asSeller) "seller" else "customer",
                phone = phone?.takeIf { it.isNotBlank() },
                storeName = storeName?.takeIf { it.isNotBlank() },
            ),
        )
        if (!r.isSuccessful) return Result.failure(Exception(r.failureMessage()))
        return Result.success(Unit)
    }

    suspend fun logout(): Result<Unit> {
        val refresh = tokenStore.refreshToken() ?: run {
            tokenStore.clear()
            return Result.success(Unit)
        }
        val r = api.logout(LogoutRequest(refresh))
        tokenStore.clear()
        if (!r.isSuccessful && r.code() != 401) {
            return Result.failure(Exception(r.failureMessage()))
        }
        return Result.success(Unit)
    }

    suspend fun categories(): Result<List<CategoryDto>> {
        val r = api.categories()
        if (!r.isSuccessful) return Result.failure(Exception(r.failureMessage()))
        return Result.success(r.body() ?: emptyList())
    }

    suspend fun products(
        page: Int,
        categoryId: Int?,
        search: String?,
        sort: String,
    ): Result<ProductPageDto> {
        val r = api.products(
            page = page,
            perPage = 20,
            categoryId = categoryId,
            search = search?.takeIf { it.isNotBlank() },
            sort = sort,
        )
        if (!r.isSuccessful) return Result.failure(Exception(r.failureMessage()))
        val body = r.body() ?: return Result.failure(Exception("Empty response"))
        return Result.success(body)
    }

    suspend fun product(id: Int): Result<ProductDto> {
        val r = api.product(id)
        if (!r.isSuccessful) return Result.failure(Exception(r.failureMessage()))
        val body = r.body() ?: return Result.failure(Exception("Not found"))
        return Result.success(body)
    }

    suspend fun me(): Result<UserProfileDto> {
        val r = api.me()
        if (!r.isSuccessful) return Result.failure(Exception(r.failureMessage()))
        val body = r.body() ?: return Result.failure(Exception("Empty response"))
        return Result.success(body)
    }

    suspend fun wishlist(): Result<WishlistResponse> {
        val r = api.wishlist()
        if (!r.isSuccessful) return Result.failure(Exception(r.failureMessage()))
        val body = r.body() ?: return Result.failure(Exception("Empty response"))
        return Result.success(body)
    }

    suspend fun wishlistAdd(productId: Int): Result<Unit> {
        val r = api.wishlistAdd(productId)
        if (!r.isSuccessful) return Result.failure(Exception(r.failureMessage()))
        return Result.success(Unit)
    }

    suspend fun wishlistRemove(productId: Int): Result<Unit> {
        val r = api.wishlistRemove(productId)
        if (!r.isSuccessful && r.code() != 204) {
            return Result.failure(Exception(r.failureMessage()))
        }
        return Result.success(Unit)
    }

    suspend fun orders(): Result<List<OrderDto>> {
        val r = api.orders()
        if (!r.isSuccessful) return Result.failure(Exception(r.failureMessage()))
        return Result.success(r.body().orEmpty())
    }

    suspend fun createOrder(lines: List<OrderLineRequest>): Result<OrderDto> {
        val r = api.createOrder(CreateOrderRequest(lines))
        if (!r.isSuccessful) return Result.failure(Exception(r.failureMessage()))
        val body = r.body() ?: return Result.failure(Exception("Empty response"))
        return Result.success(body)
    }
}
