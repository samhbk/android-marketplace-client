package com.marketplace.app.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.marketplace.app.data.remote.OrderLineRequest
import com.marketplace.app.data.remote.ProductDto
import com.marketplace.app.data.repository.MarketplaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val product: ProductDto? = null,
    val loading: Boolean = true,
    val error: String? = null,
    val wishlisted: Boolean? = null,
    val orderMessage: String? = null,
    val ordering: Boolean = false,
)

class ProductDetailViewModel(
    private val repository: MarketplaceRepository,
    private val productId: Int,
    private val loggedIn: Boolean,
) : ViewModel() {

    private val _state = MutableStateFlow(ProductDetailUiState())
    val state: StateFlow<ProductDetailUiState> = _state.asStateFlow()

    init {
        load()
        if (loggedIn) {
            checkWishlist()
        }
    }

    private fun load() {
        viewModelScope.launch {
            repository.product(productId).fold(
                onSuccess = { p -> _state.update { it.copy(product = p, loading = false, error = null) } },
                onFailure = { e ->
                    _state.update { it.copy(loading = false, error = e.message) }
                },
            )
        }
    }

    private fun checkWishlist() {
        viewModelScope.launch {
            repository.wishlist().fold(
                onSuccess = { w ->
                    val inList = w.items.any { it.id == productId }
                    _state.update { it.copy(wishlisted = inList) }
                },
                onFailure = { _state.update { it.copy(wishlisted = false) } },
            )
        }
    }

    fun toggleWishlist() {
        if (!loggedIn) return
        viewModelScope.launch {
            val current = _state.value.wishlisted ?: false
            val r = if (current) {
                repository.wishlistRemove(productId)
            } else {
                repository.wishlistAdd(productId)
            }
            r.fold(
                onSuccess = {
                    _state.update { it.copy(wishlisted = !current) }
                },
                onFailure = { e -> _state.update { it.copy(error = e.message) } },
            )
        }
    }

    fun placeOrder(quantity: Int) {
        if (!loggedIn) {
            _state.update { it.copy(orderMessage = "Sign in to place an order") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(ordering = true, orderMessage = null) }
            repository.createOrder(
                listOf(
                    OrderLineRequest(
                        productId = productId,
                        quantity = quantity,
                    ),
                ),
            ).fold(
                onSuccess = { order ->
                    _state.update {
                        it.copy(
                            ordering = false,
                            orderMessage = "Order #${order.id} placed (${order.status})",
                        )
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(ordering = false, orderMessage = e.message) }
                },
            )
        }
    }

    fun clearMessages() {
        _state.update { it.copy(orderMessage = null, error = null) }
    }

    companion object {
        fun factory(
            repository: MarketplaceRepository,
            productId: Int,
            loggedIn: Boolean,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ProductDetailViewModel(repository, productId, loggedIn) as T
        }
    }
}
