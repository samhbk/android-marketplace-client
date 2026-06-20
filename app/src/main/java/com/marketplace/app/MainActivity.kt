package com.marketplace.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.marketplace.app.ui.auth.LoginScreen
import com.marketplace.app.ui.auth.RegisterScreen
import com.marketplace.app.ui.product.ProductDetailScreen
import com.marketplace.app.ui.shop.ShopScreen
import com.marketplace.app.ui.tabs.AccountScreen
import com.marketplace.app.ui.tabs.OrdersScreen
import com.marketplace.app.ui.tabs.WishlistScreen
import com.marketplace.app.ui.theme.MarketplaceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as MarketplaceApplication
        setContent {
            MarketplaceTheme {
                val nav = rememberNavController()
                val loggedIn by app.container.tokenStore.isLoggedIn.collectAsState()
                val backStackEntry by nav.currentBackStackEntryAsState()
                val route = backStackEntry?.destination?.route
                val showBar = route in setOf("shop", "wishlist", "orders", "account")

                Scaffold(
                    bottomBar = {
                        if (showBar) {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = route == "shop",
                                    onClick = {
                                        nav.navigate("shop") {
                                            popUpTo(nav.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Shop") },
                                    label = { Text("Shop") },
                                )
                                NavigationBarItem(
                                    selected = route == "wishlist",
                                    onClick = {
                                        nav.navigate("wishlist") {
                                            popUpTo(nav.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Wishlist") },
                                    label = { Text("Wishlist") },
                                )
                                NavigationBarItem(
                                    selected = route == "orders",
                                    onClick = {
                                        nav.navigate("orders") {
                                            popUpTo(nav.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.List, contentDescription = "Orders") },
                                    label = { Text("Orders") },
                                )
                                NavigationBarItem(
                                    selected = route == "account",
                                    onClick = {
                                        nav.navigate("account") {
                                            popUpTo(nav.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Person, contentDescription = "Account") },
                                    label = { Text("Account") },
                                )
                            }
                        }
                    },
                ) { padding ->
                    NavHost(
                        navController = nav,
                        startDestination = "shop",
                        modifier = Modifier.padding(padding),
                    ) {
                        composable("shop") {
                            ShopScreen(
                                repository = app.container.repository,
                                onProductClick = { id -> nav.navigate("product/$id") },
                            )
                        }
                        composable("wishlist") {
                            WishlistScreen(
                                repository = app.container.repository,
                                loggedIn = loggedIn,
                                onSignIn = { nav.navigate("login") },
                                onProduct = { id -> nav.navigate("product/$id") },
                            )
                        }
                        composable("orders") {
                            OrdersScreen(
                                repository = app.container.repository,
                                loggedIn = loggedIn,
                                onSignIn = { nav.navigate("login") },
                            )
                        }
                        composable("account") {
                            AccountScreen(
                                repository = app.container.repository,
                                loggedIn = loggedIn,
                                onLogin = { nav.navigate("login") },
                                onRegister = { nav.navigate("register") },
                                onLogout = { },
                            )
                        }
                        composable("login") {
                            LoginScreen(
                                repository = app.container.repository,
                                onLoggedIn = { nav.popBackStack() },
                                onRegister = { nav.navigate("register") },
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                repository = app.container.repository,
                                onRegistered = {
                                    nav.navigate("login") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                },
                                onBackToLogin = { nav.popBackStack() },
                            )
                        }
                        composable(
                            route = "product/{id}",
                            arguments = listOf(
                                navArgument("id") { type = NavType.IntType },
                            ),
                        ) { entry ->
                            val id = entry.arguments?.getInt("id") ?: return@composable
                            ProductDetailScreen(
                                productId = id,
                                repository = app.container.repository,
                                loggedIn = loggedIn,
                                onBack = { nav.popBackStack() },
                                onSignIn = { nav.navigate("login") },
                            )
                        }
                    }
                }
            }
        }
    }
}
