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

    private fun generateMockOrders(): List<Order> {
        val calendar = Calendar.getInstance()
        
        // Order 1: Pending - Pho Thin
        val order1 = Order(
            id = "RF2849",
            status = "PENDING",
            total = 125000,
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
                    price = 7000
                )
            ),
            createdAt = Timestamp.now()
        )

        // Order 2: Processing - Sushi
        val order2 = Order(
            id = "RF2901",
            status = "PROCESSING",
            total = 350000,
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
            total = 210000,
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
        
        // Order 4: Delivering - Bún Chả
        val order4 = Order(
            id = "RF3005",
            status = "DELIVERING",
            total = 85000,
             items = listOf(
                OrderItem(
                    foodName = "Bún Chả Hà Nội",
                    foodImage = "https://lh3.googleusercontent.com/aida-public/AB6AXuBC2bB2-tP5tHqG2fS1wJ7g0tL6xO8zN3vW9lM5oP4qR0yK1uE7iF8jC2aD3bX4eY5fH6iJ7kL8mN9oP0qR1sT2uU3dV4wX5yZ6A7bC8dE9fG0hI1jK2lM3nP4oQ5rS6tU7vW8xY9zB1cE2fG3hJ4iK5lM6nP7oQ8rS9tU0vW1xY2zB3cE4fG5hJ6iK7lM8nP9oQ0rS1tU2vW3xY4zB5cE6fG7hJ8iK9lM0nP1oQ2rS3tU4vW5xY6zB7cE8fG9", // Placeholder if specific unavailable
                    quantity = 2,
                    price = 42500
                )
            ),
            createdAt = Timestamp.now()
        )


        return listOf(order1, order2, order3, order4)
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
