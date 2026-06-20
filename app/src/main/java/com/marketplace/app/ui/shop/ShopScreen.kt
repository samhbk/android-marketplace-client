package com.marketplace.app.ui.shop

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.marketplace.app.data.remote.ProductDto
import com.marketplace.app.util.ImageUrl
import com.marketplace.app.util.Money

@Composable
fun ShopScreen(
    repository: com.marketplace.app.data.repository.MarketplaceRepository,
    onProductClick: (Int) -> Unit,
) {
    val vm: ShopViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                ShopViewModel(repository) as T
        },
    )
    val state by vm.state.collectAsState()

    Box(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = state.search,
                onValueChange = { vm.setSearch(it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search products") },
                singleLine = true,
            )
            IconButton(onClick = { vm.applySearch() }) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = state.sort == "newest",
                onClick = { vm.setSort("newest") },
                label = { Text("Newest") },
            )
            FilterChip(
                selected = state.sort == "price_asc",
                onClick = { vm.setSort("price_asc") },
                label = { Text("Price ↑") },
            )
            FilterChip(
                selected = state.sort == "price_desc",
                onClick = { vm.setSort("price_desc") },
                label = { Text("Price ↓") },
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FilterChip(
                    selected = state.selectedCategoryId == null,
                    onClick = { vm.setCategory(null) },
                    label = { Text("All") },
                )
            }
            lazyItems(state.categories, key = { it.id }) { c ->
                FilterChip(
                    selected = state.selectedCategoryId == c.id,
                    onClick = { vm.setCategory(c.id) },
                    label = { Text(c.name) },
                )
            }
        }

        if (state.error != null) {
            Text(
                state.error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp),
            )
            Button(onClick = { vm.clearError(); vm.refreshProducts(reset = true) }) {
                Text("Retry")
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            lazyItems(state.products, key = { it.id }) { p ->
                ProductRow(p, onClick = { onProductClick(p.id) })
            }
            item {
                if (state.loadingMore) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (state.hasMore && state.products.isNotEmpty()) {
                    Button(
                        onClick = { vm.loadMore() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Load more")
                    }
                }
            }
        }
    }

        if (state.loading && state.products.isEmpty()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }
}

@Composable
private fun ProductRow(product: ProductDto, onClick: () -> Unit) {
    val img = product.images.minByOrNull { it.sortOrder } ?: product.images.firstOrNull()
    Card(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(Modifier.padding(8.dp)) {
            AsyncImage(
                model = ImageUrl.resolve(img?.path),
                contentDescription = product.title,
                modifier = Modifier
                    .height(88.dp)
                    .fillMaxWidth(0.28f),
                contentScale = ContentScale.Crop,
            )
            Column(Modifier.padding(start = 12.dp)) {
                Text(product.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    Money.formatMinor(product.priceMinor, product.currency),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                product.seller?.let {
                    Text(it.displayName, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
