package com.muatrenthenang.resfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muatrenthenang.resfood.data.model.Order
import com.muatrenthenang.resfood.data.model.OrderItem
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class OrderListViewModel : ViewModel() {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    fun loadOrders(status: String) {
        viewModelScope.launch {
            // Mock Data Generation
            val mockOrders = generateMockOrders()
            
            // Filtering Logic
            _orders.value = when (status) {
                "all" -> mockOrders
                "pending" -> mockOrders.filter { it.status == "PENDING" }
                "processing" -> mockOrders.filter { it.status == "PROCESSING" }
                "delivering" -> mockOrders.filter { it.status == "DELIVERING" }
                "review" -> mockOrders.filter { it.status == "COMPLETED" } // For review purpose
                "history" -> mockOrders.filter { it.status == "COMPLETED" || it.status == "CANCELLED" || it.status == "REJECTED" }
                else -> mockOrders
            }
        }
    }

    fun getOrder(orderId: String): Order? {
        return _orders.value.find { it.id == orderId }
    }

    private fun generateMockOrders(): List<Order> {
        val calendar = Calendar.getInstance()
        
        // Order 1: Pending - Pho Thin
        val order1 = Order(
            id = "RF2849",
            status = "PENDING",
            total = 135000,
            subtotal = 120000,
            deliveryFee = 15000,
            discount = 0,
            items = listOf(
                OrderItem(
                    foodName = "Phở Thìn Lò Đúc - Đặc biệt",
                    foodImage = "https://lh3.googleusercontent.com/aida-public/AB6AXuAr4eSOeU5UWs0gu9cDbzQ44BTMykZefwj8FUnWuBszLqv9v6_oieUHsreGq95yftXt0Rl4xk9bAZFzz4TYHt7MyOiCyUUlp_R36WXSJx5ju8z2zxaBC4UQPyYEYGSmhBk20mhuU8dv-RfxNuOVL30MYjSmnZuT1rMqI0tUqQIJMMdYvYMpOzvQ_TadFYfm44rFvQvEm7LWshMk4wd64M3sOxpW6jsaEpoQAQPbzvOXTaDF7_fO-GRJZCrLlxD9IwjVvvAnPBow6fM",
                    quantity = 2,
                    price = 45000
                ),
                OrderItem(
                    foodName = "Quẩy giòn",
                    foodImage = "", // Placeholder
                    quantity = 5,
                    price = 6000
                )
            ),
            createdAt = Timestamp.now()
        )

        // Order 2: Processing - Sushi
        val order2 = Order(
            id = "RF2901",
            status = "PROCESSING",
            total = 380000,
            subtotal = 350000,
            deliveryFee = 30000,
            discount = 0,
            items = listOf(
                OrderItem(
                    foodName = "Combo Sushi Tình Yêu",
                     foodImage = "https://lh3.googleusercontent.com/aida-public/AB6AXuBUA0hopLRSc6mDSNm-1IPVgWB6pDXHmB0sBwAKbG_zlIwGG4ypDNVWMcSSnsAYPSnOrAa0qkFV9CowAeB5rKSHA2SaPkgymKUwAlEAAjv4XFyzzHctaXFdV_Th_HYKCh8WS8LPGmgBSL8KO2L2XvMPlIuwIj9yuCu4f_GxJehTDG8twGIZI17a1LWLsPtM_T1y4a8F4vYmxT218OgqWBeyR4LW8pXrGopaTCcOVAIFXfujTv9Mek_Q-8FxdDsucxrgQK70au0GlLc",
                     quantity = 1,
                     price = 350000
                )
            ),
             createdAt = Timestamp.now()
        )

         // Order 3: Completed - Pizza
        calendar.add(Calendar.DAY_OF_YEAR, -2)
        val order3 = Order(
            id = "RF1102",
            status = "COMPLETED",
            total = 225000,
            subtotal = 210000,
            deliveryFee = 15000,
            discount = 0,
            items = listOf(
                OrderItem(
                    foodName = "Pizza Hải Sản Phô Mai",
                    foodImage = "https://lh3.googleusercontent.com/aida-public/AB6AXuBAj_GiOAJ3dzXuCVDVyE6T6ZwFDbS2ERxhiJ_MOOOj2ScNQ0ZC7tANoc9CtPHSoEfAUAJocV6j3bQs8NDYEBHPaGeVnQJRbW7-9Xb_lEtIJlmKR4-5C7_vftWpInLmDIQ33XUg30zOzF9fEr_Ql-AiShCMKhaR5_O_AH86-x5mCnDHKm3oWNHQgqX61lHlnN-GzIqXEBjwRj59p6aO8QkSZ46FXixJeihMGgVifJtVHvShGIj7-Sy3roR5HlXSdjIdlUg3zHa13bc",
                    quantity = 1,
                    price = 210000
                )
            ),
            createdAt = Timestamp(calendar.time)
        )
        
        // Order 5: Delivering - Bun Bo Hue (HTML Data)
        val order5 = Order(
            id = "RES-10293",
            status = "DELIVERING",
            total = 135000,
            subtotal = 135000,
            deliveryFee = 15000,
            discount = 15000,
            items = listOf(
                OrderItem(
                    foodName = "Bún Bò Huế Đặc Biệt",
                    foodImage = "https://lh3.googleusercontent.com/aida-public/AB6AXuDr__mHrN-ONr2yuiHWrTWP2fNv6TD5HBX-538mOGNnqHsxgH7x9nN2wKndku8XjKzHZngaNdG5cNpX3H5HvvVLqYvyXlcz6TwSYFYX0VtpeLsHRjHrOMv1yc842P9kJGnqVR5zrs0iTlslyzbm757Fkw7LwPMcL5ByRnLc431Zw1dKU2lsGYZf65ePwiBy14pfxGjzPIFiSOMl5dmkrRkYnB1Ydm7OJKoTGAjQkELyL2p2789LB1H7VtwWne2ZF1cj6mZNfOiqSjI",
                    quantity = 1,
                    price = 55000,
                    note = "Không hành, nhiều ớt sa tế"
                ),
                OrderItem(
                    foodName = "Trà Tắc Khổng Lồ",
                    foodImage = "https://lh3.googleusercontent.com/aida-public/AB6AXuAhy73oKM9yTm7N7PxrDFds8Euzsvu6_EvA1WJaXVXBfHc9jn9JahtAIlQQFTURde3RDw0uFYg471FqfalstBeW8u3ORhMWY2yFIn7har0g7AyPscFsfyYtL1MoIQNagbBVZJvJqQ43kYEhEiBJ6T06js28snvOyIjEghaWzsOQ1O2msx4cz5xkjNGGqUyATNbg7LlMGuM4Tu_Vmv3bGTdWw3eAmIk_2NZxuBE0O1C9mPYtNhnzHhXnv1ivAMfEdgt2u3h4LOqweY8",
                    quantity = 2,
                    price = 40000,
                    note = "50% đường, 100% đá"
                )
            ),
            createdAt = Timestamp.now()
        )
        
        return listOf(order1, order2, order3, order5)
    }

    fun callRestaurant(orderId: String) {
        // Logic to make a call
    }

    fun cancelOrder(orderId: String) {
        // Logic to cancel order
    }

    fun reviewOrder(orderId: String) {
        // Logic to review
    }
    
    fun reOrder(orderId: String) {
        // Logic to reorder
    }
}
