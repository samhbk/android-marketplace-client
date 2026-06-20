package com.marketplace.app.ui.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.marketplace.app.data.remote.OrderDto
import com.marketplace.app.data.remote.ProductDto
import com.marketplace.app.data.remote.UserProfileDto
import com.marketplace.app.data.repository.MarketplaceRepository
import com.marketplace.app.util.ImageUrl
import com.marketplace.app.util.Money
import kotlinx.coroutines.launch

@Composable
fun WishlistScreen(
    repository: MarketplaceRepository,
    loggedIn: Boolean,
    onSignIn: () -> Unit,
    onProduct: (Int) -> Unit,
) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var products by remember { mutableStateOf<List<ProductDto>>(emptyList()) }

    if (!loggedIn) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Sign in to save items to your wishlist.")
            Button(onClick = onSignIn, modifier = Modifier.padding(top = 16.dp)) {
                Text("Sign in")
            }
        }
        return
    }

    LaunchedEffect(Unit) {
        loading = true
        repository.wishlist().fold(
            onSuccess = { products = it.items; error = null },
            onFailure = { error = it.message },
        )
        loading = false
    }

    when {
        loading -> BoxCenterLoading()
        error != null -> Text(error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
        else -> LazyColumn(
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            lazyItems(products, key = { it.id }) { p ->
                WishlistRow(p, onClick = { onProduct(p.id) })
            }
        }
    }
}

@Composable
private fun WishlistRow(product: ProductDto, onClick: () -> Unit) {
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
                    .height(72.dp)
                    .fillMaxWidth(0.22f),
                contentScale = ContentScale.Crop,
            )
            Column(Modifier.padding(start = 12.dp)) {
                Text(product.title, style = MaterialTheme.typography.titleMedium)
                Text(Money.formatMinor(product.priceMinor, product.currency))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    repository: MarketplaceRepository,
    loggedIn: Boolean,
    onSignIn: () -> Unit,
) {
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var orders by remember { mutableStateOf<List<OrderDto>>(emptyList()) }
    val scope = rememberCoroutineScope()

    suspend fun loadOrders() {
        repository.orders().fold(
            onSuccess = { orders = it; error = null },
            onFailure = { error = it.message },
        )
    }

    if (!loggedIn) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Sign in to see your orders.")
            Button(onClick = onSignIn, modifier = Modifier.padding(top = 16.dp)) {
                Text("Sign in")
            }
        }
        return
    }

    LaunchedEffect(Unit) {
        loading = true
        loadOrders()
        loading = false
    }

    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = {
            scope.launch {
                refreshing = true
                loadOrders()
                refreshing = false
            }
        },
        modifier = Modifier.fillMaxSize(),
    ) {
        when {
            loading -> BoxCenterLoading()
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (error != null) {
                    item(key = "error") {
                        Text(
                            error!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                } else if (orders.isEmpty()) {
                    item(key = "empty") {
                        Text("No orders yet.", modifier = Modifier.padding(8.dp))
                    }
                } else {
                    lazyItems(orders, key = { it.id }) { o ->
                        OrderCard(o)
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: OrderDto) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("Order #${order.id}", style = MaterialTheme.typography.titleMedium)
            Text("Status: ${order.status}", style = MaterialTheme.typography.bodyMedium)
            Text(
                Money.formatMinor(order.totalMinor, order.currency),
                color = MaterialTheme.colorScheme.primary,
            )
            Text(order.placedAt, style = MaterialTheme.typography.bodySmall)
            order.items.forEach { line ->
                Text(
                    "${line.quantity}× ${line.productTitle}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
fun AccountScreen(
    repository: MarketplaceRepository,
    loggedIn: Boolean,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onLogout: () -> Unit,
) {
    var profile by remember { mutableStateOf<UserProfileDto?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    if (loggedIn) {
        LaunchedEffect(Unit) {
            loading = true
            repository.me().fold(
                onSuccess = { profile = it; error = null },
                onFailure = { error = it.message },
            )
            loading = false
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text("Account", style = MaterialTheme.typography.headlineMedium)
        when {
            !loggedIn -> {
                Text("Sign in to manage your profile.", modifier = Modifier.padding(top = 16.dp))
                Button(onClick = onLogin, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Login")
                }
                Button(onClick = onRegister, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Register")
                }
            }
            loading -> BoxCenterLoading()
            error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
            profile != null -> {
                val p = profile!!
                Text(p.displayName, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp))
                Text(p.email, style = MaterialTheme.typography.bodyMedium)
                p.phone?.let { Text(it) }
                p.roles.takeIf { it.isNotEmpty() }?.let { Text("Roles: ${it.joinToString()}") }
                p.seller?.let { s ->
                    Text("Store: ${s.storeName}", modifier = Modifier.padding(top = 8.dp))
                    s.bio?.let { Text(it) }
                }
                Button(
                    onClick = {
                        scope.launch {
                            repository.logout()
                            profile = null
                            error = null
                            onLogout()
                        }
                    },
                    modifier = Modifier.padding(top = 24.dp),
                ) {
                    Text("Log out")
                }
            }
            else -> {
                if (loggedIn && !loading && error == null) {
                    Text("Unable to load profile.", modifier = Modifier.padding(top = 16.dp))
                }
            }
        }
    }
}

@Composable
private fun BoxCenterLoading() {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
    }
}
