package com.marketplace.app.data.remote

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    val token: String,
    @SerializedName("refresh_token") val refreshToken: String,
)

data class LoginRequest(val email: String, val password: String)

data class RefreshRequest(@SerializedName("refresh_token") val refreshToken: String)

data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String,
    val role: String = "customer",
    val phone: String? = null,
    val storeName: String? = null,
)

data class RegisterResponse(val id: Int, val email: String)

data class LogoutRequest(@SerializedName("refresh_token") val refreshToken: String)

data class ApiError(val error: String?)

data class CategoryDto(
    val id: Int,
    val name: String,
    val slug: String,
    val description: String? = null,
    @SerializedName("isActive") val isActive: Boolean = true,
    @SerializedName("parentId") val parentId: Int? = null,
)

data class ProductSellerDto(val id: Int, val displayName: String)

data class ProductCategoryDto(val id: Int, val name: String, val slug: String)

data class ProductImageDto(val id: Int, val path: String, val sortOrder: Int)

data class ProductDto(
    val id: Int,
    val title: String,
    val slug: String,
    @SerializedName("priceMinor") val priceMinor: Int,
    val currency: String,
    val status: String,
    val category: ProductCategoryDto? = null,
    val seller: ProductSellerDto? = null,
    val images: List<ProductImageDto> = emptyList(),
    @SerializedName("createdAt") val createdAt: String? = null,
    val description: String? = null,
)

data class ProductPageDto(
    val items: List<ProductDto>,
    val total: Int,
    val page: Int,
    @SerializedName("perPage") val perPage: Int,
)

data class WishlistResponse(val items: List<ProductDto>)

data class OrderLineRequest(val productId: Int, val quantity: Int)

data class CreateOrderRequest(val items: List<OrderLineRequest>)

data class OrderItemDto(
    val id: Int,
    @SerializedName("productId") val productId: Int?,
    @SerializedName("productTitle") val productTitle: String,
    @SerializedName("unitPriceMinor") val unitPriceMinor: Int,
    val quantity: Int,
)

data class OrderDto(
    val id: Int,
    val status: String,
    val currency: String,
    @SerializedName("totalMinor") val totalMinor: Int,
    @SerializedName("placedAt") val placedAt: String,
    val items: List<OrderItemDto> = emptyList(),
)

data class UserProfileDto(
    val id: Int,
    val email: String,
    val displayName: String,
    val phone: String? = null,
    val roles: List<String> = emptyList(),
    val seller: SellerProfileDto? = null,
)

data class SellerProfileDto(
    val storeName: String,
    val bio: String? = null,
    val verified: Boolean = false,
)
