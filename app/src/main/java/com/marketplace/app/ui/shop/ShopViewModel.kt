package com.marketplace.app.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketplace.app.data.remote.CategoryDto
import com.marketplace.app.data.remote.ProductDto
import com.marketplace.app.data.repository.MarketplaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShopUiState(
    val categories: List<CategoryDto> = emptyList(),
    val products: List<ProductDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val error: String? = null,
    val selectedCategoryId: Int? = null,
    val search: String = "",
    val sort: String = "newest",
    val hasMore: Boolean = true,
)

class ShopViewModel(
    private val repository: MarketplaceRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ShopUiState())
    val state: StateFlow<ShopUiState> = _state.asStateFlow()

    init {
        refreshCategories()
        refreshProducts(reset = true)
    }

    fun refreshCategories() {
        viewModelScope.launch {
            repository.categories().fold(
                onSuccess = { list -> _state.update { it.copy(categories = list) } },
                onFailure = { e -> _state.update { it.copy(error = e.message) } },
            )
        }
    }

    fun refreshProducts(reset: Boolean) {
        viewModelScope.launch {
            if (reset) {
                _state.update { it.copy(loading = true, error = null, page = 1, products = emptyList()) }
            } else {
                _state.update { it.copy(loadingMore = true, error = null) }
            }
            val s = _state.value
            val page = if (reset) 1 else s.page
            repository.products(
                page = page,
                categoryId = s.selectedCategoryId,
                search = s.search,
                sort = s.sort,
            ).fold(
                onSuccess = { pageDto ->
                    _state.update {
                        val merged = if (reset) pageDto.items else it.products + pageDto.items
                        val nextPage = pageDto.page + 1
                        val hasMore = merged.size < pageDto.total
                        it.copy(
                            products = merged,
                            total = pageDto.total,
                            page = nextPage,
                            loading = false,
                            loadingMore = false,
                            hasMore = hasMore,
                        )
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            loading = false,
                            loadingMore = false,
                            error = e.message,
                        )
                    }
                },
            )
        }
    }

    fun setCategory(id: Int?) {
        _state.update { it.copy(selectedCategoryId = id) }
        refreshProducts(reset = true)
    }

    fun setSearch(q: String) {
        _state.update { it.copy(search = q) }
    }

    fun applySearch() {
        refreshProducts(reset = true)
    }

    fun setSort(sort: String) {
        _state.update { it.copy(sort = sort) }
        refreshProducts(reset = true)
    }

    fun loadMore() {
        val s = _state.value
        if (!s.hasMore || s.loading || s.loadingMore) return
        viewModelScope.launch {
            _state.update { it.copy(loadingMore = true, error = null) }
            repository.products(
                page = s.page,
                categoryId = s.selectedCategoryId,
                search = s.search,
                sort = s.sort,
            ).fold(
                onSuccess = { pageDto ->
                    _state.update {
                        val merged = it.products + pageDto.items
                        val hasMore = merged.size < pageDto.total
                        it.copy(
                            products = merged,
                            total = pageDto.total,
                            page = pageDto.page + 1,
                            loadingMore = false,
                            hasMore = hasMore,
                        )
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(loadingMore = false, error = e.message)
                    }
                },
            )
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
