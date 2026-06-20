package com.marketplace.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MarketplaceApi {

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<TokenResponse>

    @POST("api/auth/logout")
    suspend fun logout(@Body body: LogoutRequest): Response<Unit>

    @GET("api/me")
    suspend fun me(): Response<UserProfileDto>

    @GET("api/categories")
    suspend fun categories(): Response<List<CategoryDto>>

    @GET("api/products")
    suspend fun products(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 20,
        @Query("categoryId") categoryId: Int? = null,
        @Query("search") search: String? = null,
        @Query("sort") sort: String = "newest",
    ): Response<ProductPageDto>

    @GET("api/products/{id}")
    suspend fun product(@Path("id") id: Int): Response<ProductDto>

    @GET("api/wishlist")
    suspend fun wishlist(): Response<WishlistResponse>

    @POST("api/wishlist/products/{productId}")
    suspend fun wishlistAdd(@Path("productId") productId: Int): Response<Unit>

    @DELETE("api/wishlist/products/{productId}")
    suspend fun wishlistRemove(@Path("productId") productId: Int): Response<Unit>

    @GET("api/orders")
    suspend fun orders(): Response<List<OrderDto>>

    @POST("api/orders")
    suspend fun createOrder(@Body body: CreateOrderRequest): Response<OrderDto>
}
