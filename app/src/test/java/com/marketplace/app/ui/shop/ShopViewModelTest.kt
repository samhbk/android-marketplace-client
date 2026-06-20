package com.marketplace.app.ui.shop

import com.marketplace.app.data.remote.ProductDto
import com.marketplace.app.data.remote.ProductPageDto
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
class ShopViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: MarketplaceRepository

    private val sampleProduct = ProductDto(
        id = 1,
        title = "Sample",
        slug = "sample",
        priceMinor = 1000,
        currency = "EUR",
        status = "active",
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        coEvery { repository.categories() } returns Result.success(emptyList())
        coEvery {
            repository.products(page = 1, categoryId = null, search = "", sort = "newest")
        } returns Result.success(
            ProductPageDto(
                items = listOf(sampleProduct),
                total = 1,
                page = 1,
                perPage = 20,
            ),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsProducts() = runTest(testDispatcher) {
        val vm = ShopViewModel(repository)
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals(1, state.products.size)
        assertEquals("Sample", state.products.first().title)
        assertFalse(state.loading)
    }

    @Test
    fun setSort_triggersReloadWithNewSort() = runTest(testDispatcher) {
        coEvery {
            repository.products(page = 1, categoryId = null, search = "", sort = "price_asc")
        } returns Result.success(
            ProductPageDto(items = emptyList(), total = 0, page = 1, perPage = 20),
        )

        val vm = ShopViewModel(repository)
        advanceUntilIdle()

        vm.setSort("price_asc")
        advanceUntilIdle()

        assertEquals("price_asc", vm.state.value.sort)
        assertTrue(vm.state.value.products.isEmpty())
    }
}
