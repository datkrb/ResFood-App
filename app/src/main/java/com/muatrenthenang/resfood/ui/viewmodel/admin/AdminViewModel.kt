package com.muatrenthenang.resfood.ui.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muatrenthenang.resfood.data.model.Food
import com.muatrenthenang.resfood.data.repository.FoodRepository
import com.muatrenthenang.resfood.data.model.Order
import com.muatrenthenang.resfood.data.model.User
import com.muatrenthenang.resfood.data.repository.OrderRepository
import com.muatrenthenang.resfood.data.repository.PromotionRepository
import com.muatrenthenang.resfood.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val totalRevenue: Double = 1240.50, // Mock data matching HTML
    val revenueGrowth: Int = 15,
    val newOrders: Int = 12,
    val newOrdersCount: Int = 2,
    val pendingOrders: Int = 5,
    val processingOrders: Int = 8,
    val reservations: Int = 12,
    val outOfStockItems: Int = 3,
    val recentActivities: List<ActivityItem> = listOf(
        ActivityItem("Order #2033 Completed", "2 mins ago", "$45.00", ActivityType.SUCCESS),
        ActivityItem("Table 4 Reserved", "10 mins ago • 19:00 PM", "4 Pax", ActivityType.PURPLE),
        ActivityItem("Order #2034 Pending", "15 mins ago", "$12.50", ActivityType.WARNING)
    )
)

data class ActivityItem(
    val title: String,
    val subtitle: String,
    val value: String,
    val type: ActivityType
)

enum class ActivityType {
    SUCCESS, PURPLE, WARNING
}

data class FoodManagementUiState(
    val isLoading: Boolean = false,
    val foods: List<Food> = emptyList(),
    val error: String? = null,
    val filter: FoodFilter = FoodFilter.ALL
)

enum class FoodFilter {
    ALL, AVAILABLE, OUT_OF_STOCK
}

class AdminViewModel(
    private val foodRepository: FoodRepository = FoodRepository(),
    private val orderRepository: OrderRepository = OrderRepository(),
    private val userRepository: UserRepository = UserRepository(),
    private val promotionRepository: PromotionRepository = PromotionRepository()
) : ViewModel() {

    private val _dashboardUiState = MutableStateFlow(DashboardUiState())
    val dashboardUiState: StateFlow<DashboardUiState> = _dashboardUiState.asStateFlow()

    private val _foodManagementUiState = MutableStateFlow(FoodManagementUiState())
    val foodManagementUiState: StateFlow<FoodManagementUiState> = _foodManagementUiState.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _customers = MutableStateFlow<List<User>>(emptyList())
    val customers: StateFlow<List<User>> = _customers.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        loadFoods()
        loadOrders()
        loadCustomers()
    }

    private fun loadFoods() {
        viewModelScope.launch {
            _foodManagementUiState.value = _foodManagementUiState.value.copy(isLoading = true, error = null)
            foodRepository.getFoods().onSuccess { foods ->
                _foodManagementUiState.value = _foodManagementUiState.value.copy(isLoading = false, foods = foods)
                updateDashboardStats() // Update stats like out of stock
            }.onFailure {
                _foodManagementUiState.value = _foodManagementUiState.value.copy(isLoading = false, error = it.message)
            }
        }
    }

    private fun loadOrders() {
        viewModelScope.launch {
            orderRepository.getAllOrdersFlow().collect { orderList ->
                _orders.value = orderList
                updateDashboardStats()
            }
        }
    }

    private fun loadCustomers() {
        viewModelScope.launch {
            userRepository.getAllCustomers().onSuccess { userList ->
                _customers.value = userList
            }
        }
    }

    private fun updateDashboardStats() {
        val orderList = _orders.value
        val foodList = _foodManagementUiState.value.foods

        val totalRevenue = orderList.filter { it.status == "COMPLETED" }.sumOf { it.total.toDouble() }
        val newOrdersCount = orderList.count { it.status == "PENDING" } // Define NEW as PENDING for now
        val pendingOrders = orderList.count { it.status == "PENDING" }
        val processingOrders = orderList.count { it.status == "PROCESSING" }
        val outOfStockItems = foodList.count { !it.isAvailable }

        _dashboardUiState.value = _dashboardUiState.value.copy(
            totalRevenue = totalRevenue,
            newOrders = newOrdersCount,
            newOrdersCount = newOrdersCount,
            pendingOrders = pendingOrders,
            processingOrders = processingOrders,
            outOfStockItems = outOfStockItems,
            recentActivities = orderList.take(5).map { order ->
                ActivityItem(
                    title = "Order #${order.id.takeLast(5)}",
                    subtitle = order.status,
                    value = "${order.total}đ",
                    type = when(order.status) {
                        "COMPLETED" -> ActivityType.SUCCESS
                        "PENDING" -> ActivityType.WARNING
                        else -> ActivityType.PURPLE
                    }
                )
            }
        )
    }

    fun setFoodFilter(filter: FoodFilter) {
        _foodManagementUiState.value = _foodManagementUiState.value.copy(filter = filter)
    }

    fun getFilteredFoods(): List<Food> {
        val state = _foodManagementUiState.value
        return when (state.filter) {
            FoodFilter.ALL -> state.foods
            FoodFilter.AVAILABLE -> state.foods.filter { it.isAvailable }
            FoodFilter.OUT_OF_STOCK -> state.foods.filter { !it.isAvailable }
        }
    }

    fun deleteFood(foodId: String) {
         viewModelScope.launch {
            foodRepository.deleteFood(foodId).onSuccess {
                loadFoods()
            }
        }
    }
    
    fun approveOrder(orderId: String) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, "PROCESSING")
        }
    }

    fun rejectOrder(orderId: String) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, "REJECTED")
        }
    }

    fun completeOrder(orderId: String) {
        viewModelScope.launch {
             orderRepository.updateOrderStatus(orderId, "COMPLETED")
        }
    }
}

data class MockOrder(
    val id: String,
    val userName: String,
    val time: String,
    val total: Double,
    val status: String,
    val items: List<String>,
    val isTable: Boolean = false
)

data class MockCustomer(
    val name: String,
    val phone: String,
    val rank: String,
    val totalSpend: Double,
    val orderCount: Int
)
