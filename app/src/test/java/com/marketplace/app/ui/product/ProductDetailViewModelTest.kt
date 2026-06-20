package com.marketplace.app.ui.product

import com.marketplace.app.data.remote.OrderDto
import com.marketplace.app.data.remote.OrderLineRequest
import com.marketplace.app.data.remote.ProductDto
import com.marketplace.app.data.repository.MarketplaceRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: MarketplaceRepository

    private val product = ProductDto(
        id = 42,
        title = "Demo product",
        slug = "demo-product",
        priceMinor = 2500,
        currency = "EUR",
        status = "active",
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        coEvery { repository.product(42) } returns Result.success(product)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun placeOrder_setsSuccessMessageWhenLoggedIn() = runTest(testDispatcher) {
        coEvery { repository.wishlist() } returns Result.success(
            com.marketplace.app.data.remote.WishlistResponse(items = emptyList()),
        )
        coEvery {
            repository.createOrder(listOf(OrderLineRequest(productId = 42, quantity = 2)))
        } returns Result.success(
            OrderDto(
                id = 7,
                status = "pending",
                currency = "EUR",
                totalMinor = 5000,
                placedAt = "2026-01-01T00:00:00Z",
            ),
        )

        val vm = ProductDetailViewModel(repository, productId = 42, loggedIn = true)
        advanceUntilIdle()

        vm.placeOrder(quantity = 2)
        advanceUntilIdle()

        val state = vm.state.value
        assertFalse(state.ordering)
        assertTrue(state.orderMessage?.contains("Order #7") == true)
    }

    @Test
    fun placeOrder_promptsSignInWhenLoggedOut() = runTest(testDispatcher) {
        val vm = ProductDetailViewModel(repository, productId = 42, loggedIn = false)
        advanceUntilIdle()

        vm.placeOrder(quantity = 1)
        advanceUntilIdle()

        assertEquals("Sign in to place an order", vm.state.value.orderMessage)
    }
}
