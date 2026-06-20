package com.marketplace.app.ui.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.marketplace.app.data.repository.MarketplaceRepository
import com.marketplace.app.util.ImageUrl
import com.marketplace.app.util.Money

@Composable
fun ProductDetailScreen(
    productId: Int,
    repository: MarketplaceRepository,
    loggedIn: Boolean,
    onBack: () -> Unit,
    onSignIn: () -> Unit,
) {
    val vm: ProductDetailViewModel = viewModel(
        factory = ProductDetailViewModel.factory(repository, productId, loggedIn),
    )
    val state by vm.state.collectAsState()
    var qty by remember { mutableIntStateOf(1) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onBack) {
                Text("Back")
            }
            if (loggedIn) {
                IconButton(onClick = { vm.toggleWishlist() }) {
                    val fav = state.wishlisted == true
                    Icon(
                        if (fav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Wishlist",
                    )
                }
            }
        }

        if (state.loading) {
            CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            return@Column
        }

        val p = state.product
        if (p == null) {
            Text(state.error ?: "Not found")
            return@Column
        }

        val hero = p.images.minByOrNull { it.sortOrder } ?: p.images.firstOrNull()
        AsyncImage(
            model = ImageUrl.resolve(hero?.path),
            contentDescription = p.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentScale = ContentScale.Crop,
        )

        Text(p.title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 12.dp))
        Text(
            Money.formatMinor(p.priceMinor, p.currency),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        p.description?.let {
            Text(it, modifier = Modifier.padding(top = 8.dp))
        }

        Row(
            Modifier.padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = qty.toString(),
                onValueChange = { v -> v.toIntOrNull()?.takeIf { it > 0 }?.let { qty = it } },
                label = { Text("Qty") },
                singleLine = true,
            )
            Button(
                onClick = {
                    if (!loggedIn) onSignIn()
                    else vm.placeOrder(qty)
                },
                enabled = !state.ordering,
            ) {
                if (state.ordering) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp),
                    )
                } else {
                    Text(if (loggedIn) "Place order" else "Sign in to order")
                }
            }
        }

        state.orderMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
