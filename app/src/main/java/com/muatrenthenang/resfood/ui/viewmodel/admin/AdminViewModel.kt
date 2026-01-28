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
import com.muatrenthenang.resfood.data.model.Promotion
import com.muatrenthenang.resfood.data.model.TableReservation
import com.muatrenthenang.resfood.data.model.Table
import com.muatrenthenang.resfood.data.repository.TableRepository
import com.muatrenthenang.resfood.data.model.Branch
import com.muatrenthenang.resfood.data.repository.BranchRepository
import com.muatrenthenang.resfood.data.repository.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class DashboardUiState(
    val timeRange: String = "Today",
    val totalRevenue: Double = 0.0,
    val revenueGrowth: Int = 0,
    val newOrders: Int = 0,
    val newOrdersCount: Int = 0,
    val pendingOrders: Int = 0,
    val processingOrders: Int = 0,
    val reservations: Int = 0,
    val outOfStockItems: Int = 0,
    val recentActivities: List<ActivityItem> = emptyList()
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
    val filteredFoods: List<Food> = emptyList(),
    val error: String? = null,
    val selectedCategory: String = "All",
    val selectedStatus: FoodStatus = FoodStatus.ALL,
    val categories: List<String> = listOf("All"),
    val branches: List<Branch> = emptyList(),
    val selectedBranch: Branch? = null
)

enum class FoodStatus(val displayName: String) {
    ALL("Tất cả"),
    AVAILABLE("Còn hàng"),
    OUT_OF_STOCK("Hết hàng")
}

data class AnalyticsUiState(
    val filterType: AnalyticsFilterType = AnalyticsFilterType.TODAY,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis(),
    val totalRevenue: Double = 0.0,
    val totalOrders: Int = 0,
    val revenueChartData: List<Pair<String, Double>> = emptyList(), // Label -> Value
    val orderStatusData: Map<String, Int> = emptyMap(), // Status -> Count
    val topProducts: List<TopProductItem> = emptyList()
)

data class TopProductItem(
    val name: String,
    val count: Int,
    val revenue: Double
)

enum class AnalyticsFilterType {
    TODAY, WEEK, MONTH, CUSTOM
}

class AdminViewModel(
    private val foodRepository: FoodRepository = FoodRepository(),
    private val orderRepository: OrderRepository = OrderRepository(),
    private val userRepository: UserRepository = UserRepository(),
    private val promotionRepository: PromotionRepository = PromotionRepository(),
    private val tableRepository: TableRepository = TableRepository(),
    private val branchRepository: BranchRepository = BranchRepository()
) : ViewModel() {

    private val _dashboardUiState = MutableStateFlow(DashboardUiState())
    val dashboardUiState: StateFlow<DashboardUiState> = _dashboardUiState.asStateFlow()

    private val _analyticsUiState = MutableStateFlow(AnalyticsUiState())
    val analyticsUiState: StateFlow<AnalyticsUiState> = _analyticsUiState.asStateFlow()

    private val _foodManagementUiState = MutableStateFlow(FoodManagementUiState())
    val foodManagementUiState: StateFlow<FoodManagementUiState> = _foodManagementUiState.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _customers = MutableStateFlow<List<User>>(emptyList())
    val customers: StateFlow<List<User>> = _customers.asStateFlow()
    
    private val _promotions = MutableStateFlow<List<Promotion>>(emptyList())
    val promotions: StateFlow<List<Promotion>> = _promotions.asStateFlow()
    
    private val _tables = MutableStateFlow<List<Table>>(emptyList())
    val tables: StateFlow<List<Table>> = _tables.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Run seeding in background - don't block UI data loading
        // viewModelScope.launch {
        //     com.muatrenthenang.resfood.data.DataSeeder().seedAll()
        // }
        
        // Load UI data immediately without waiting for seeding
        viewModelScope.launch {
            loadOrders()
            // Initialize analytics with TODAY filter
            setAnalyticsFilter(AnalyticsFilterType.TODAY)
            loadData()
        }
    }
    
    fun refreshData() {
        viewModelScope.launch {
            loadData()
            // Re-calculate analytics on refresh using current filter
            calculateAnalytics()
        }
    }

    private suspend fun loadData() {
        _isLoading.value = true
        // Load all data in parallel to speed up start time (like Guest Main Page)
        val jobFoods = viewModelScope.async { loadFoods() }
        val jobCustomers = viewModelScope.async { loadCustomers() }
        val jobPromotions = viewModelScope.async { loadPromotions() }
        val jobTables = viewModelScope.async { loadTables() }
        val jobBranches = viewModelScope.async { loadBranches() }
        
        // Wait for all to complete
        jobFoods.await()
        jobCustomers.await()
        jobPromotions.await()
        jobTables.await()
        jobBranches.await()
        
        _isLoading.value = false
    }

    private suspend fun loadFoods() {
        _foodManagementUiState.value = _foodManagementUiState.value.copy(isLoading = true, error = null)
        foodRepository.getFoods().onSuccess { foods ->
            val categories = listOf("All") + foods.map { it.category }.distinct().filter { it.isNotEmpty() }
            _foodManagementUiState.value = _foodManagementUiState.value.copy(
                isLoading = false, 
                foods = foods,
                categories = categories
            )
            updateFilteredFoods()
            updateDashboardStats()
        }.onFailure {
            _foodManagementUiState.value = _foodManagementUiState.value.copy(isLoading = false, error = it.message)
        }
    }
    
    private fun loadOrders() {
        viewModelScope.launch {
            orderRepository.getAllOrdersFlow().collect { orderList ->
                _orders.value = orderList
                updateDashboardStats()
                calculateAnalytics()
            }
        }
    }
    
    suspend fun loadCustomers() {
         userRepository.getAllCustomers().onSuccess { userList ->
            _customers.value = userList
        }.onFailure {
            _customers.value = emptyList()
        }
    }
    
    fun deleteUser(userId: String) {
        viewModelScope.launch {
            userRepository.deleteUser(userId).onSuccess {
                loadCustomers()
            }
        }
    }
    
    fun updateUser(userId: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            userRepository.updateUser(userId, updates).onSuccess {
                loadCustomers()
            }
        }
    }
    
    private suspend fun loadPromotions() {
         promotionRepository.getAllPromotions().onSuccess { promos ->
             _promotions.value = promos
         }.onFailure {
             // Do not load mock data on failure, just log or show error if needed
             // _promotions.value = emptyList() is already default
         }
    }
    
    private suspend fun loadTables() {
        tableRepository.getAllTables().onSuccess { tableList ->
            if(tableList.isEmpty()) {
                val seed = listOf(
                    Table(name = "Bàn 01", status = "EMPTY", seats = 4),
                    Table(name = "Bàn 02", status = "EMPTY", seats = 2),
                    Table(name = "Bàn 03", status = "EMPTY", seats = 4),
                    Table(name = "Bàn 04", status = "EMPTY", seats = 6),
                )
                seed.forEach { tableRepository.createTable(it) }
                _tables.value = seed
            } else {
                _tables.value = tableList
            }
        }.onFailure {
            _tables.value = emptyList()
        }
    }

    private suspend fun loadBranches() {
        branchRepository.getBranches().onSuccess { loadedBranches ->
            _foodManagementUiState.value = _foodManagementUiState.value.copy(branches = loadedBranches)
        }
    }

    fun setAnalyticsFilter(type: AnalyticsFilterType, start: Long? = null, end: Long? = null) {
        val now = System.currentTimeMillis()
        var startDate: Long = now
        var endDate: Long = now

        when (type) {
            AnalyticsFilterType.TODAY -> {
                val calendar = java.util.Calendar.getInstance()
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                startDate = calendar.timeInMillis
                endDate = now // Until now
            }
            AnalyticsFilterType.WEEK -> {
                val calendar = java.util.Calendar.getInstance()
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -7)
                 startDate = calendar.timeInMillis
                 endDate = now
            }
            AnalyticsFilterType.MONTH -> {
                val calendar = java.util.Calendar.getInstance()
                calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                startDate = calendar.timeInMillis
                endDate = now
            }
            AnalyticsFilterType.CUSTOM -> {
                startDate = start ?: now
                endDate = end ?: now
            }
        }
        
        _analyticsUiState.value = _analyticsUiState.value.copy(
            filterType = type,
            startDate = startDate,
            endDate = endDate
        )
        calculateAnalytics()
    }

    private fun calculateAnalytics() {
        val state = _analyticsUiState.value
        val allOrders = _orders.value
        
        // Filter orders
        val filteredOrders = allOrders.filter { order ->
            val time = order.createdAt?.toDate()?.time ?: 0L
            time >= state.startDate && time <= state.endDate
        }
        
        val completedOrders = filteredOrders.filter { it.status == "COMPLETED" }

        // 1. Overview Stats
        val totalRevenue = completedOrders.sumOf { it.total.toDouble() }
        val totalOrders = completedOrders.size
        
        // 2. Chart Data 
        val chartData = if (state.filterType == AnalyticsFilterType.TODAY) {
            // Group by Hour
            completedOrders.groupBy { 
                val cal = java.util.Calendar.getInstance()
                cal.time = it.createdAt!!.toDate() 
                cal.get(java.util.Calendar.HOUR_OF_DAY)
            }.map { (hour, orders) ->
                "${hour}:00" to orders.sumOf { it.total.toDouble() }
            }.sortedBy { it.first }
        } else {
            // Group by Date (dd/MM)
            val dateFormat = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())
            completedOrders.groupBy { 
                dateFormat.format(it.createdAt!!.toDate())
            }.map { (date, orders) ->
                date to orders.sumOf { it.total.toDouble() }
            }.sortedBy { it.first } 
        }

        // 3. Order Status Distribution (All filtered)
        val statusData = filteredOrders.groupBy { it.status }
            .mapValues { it.value.size }
            
        // 4. Top Products
        val productMap = mutableMapOf<String, TopProductItem>()
        completedOrders.flatMap { it.items }.forEach { item ->
             val existing = productMap[item.foodName]
             if (existing != null) {
                 productMap[item.foodName] = existing.copy(
                     count = existing.count + item.quantity,
                     revenue = existing.revenue + (item.price * item.quantity)
                 )
             } else {
                 productMap[item.foodName] = TopProductItem(item.foodName, item.quantity, (item.price * item.quantity).toDouble())
             }
        }
        val topProducts = productMap.values.sortedByDescending { it.revenue }.take(5).toList()

        _analyticsUiState.value = state.copy(
            totalRevenue = totalRevenue,
            totalOrders = totalOrders,
            revenueChartData = chartData,
            orderStatusData = statusData,
            topProducts = topProducts
        )
    }

    private fun updateDashboardStats() {
        val orderList = _orders.value
        val foodList = _foodManagementUiState.value.foods
        val range = _dashboardUiState.value.timeRange

        // Filter orders by time range
        val now = System.currentTimeMillis()
        val filteredOrders = orderList.filter { order ->
            if (order.createdAt == null) return@filter false // Should not happen with real data
            val time = order.createdAt.toDate().time
            val diff = now - time
            when (range) {
                "Today" -> diff < 24 * 60 * 60 * 1000L
                "This Week" -> diff < 7 * 24 * 60 * 60 * 1000L
                "This Month" -> diff < 30L * 24 * 60 * 60 * 1000L
                else -> true
            }
        }

        val totalRevenue = filteredOrders.filter { it.status == "COMPLETED" }.sumOf { it.total.toDouble() }
        val newOrdersCount = filteredOrders.count { it.status == "PENDING" } 
        val pendingOrders = filteredOrders.count { it.status == "PENDING" }
        val processingOrders = filteredOrders.count { it.status == "PROCESSING" }
        val outOfStockItems = foodList.count { !it.isAvailable }
        
        // Simple mock revenue growth
        val previousSemRevenue = 1000.0 
        val revenueGrowth = if(previousSemRevenue > 0 && totalRevenue > 0) ((totalRevenue - previousSemRevenue) / previousSemRevenue * 100).toInt() else 0

        _dashboardUiState.value = _dashboardUiState.value.copy(
            totalRevenue = totalRevenue,
            revenueGrowth = revenueGrowth,
            newOrders = newOrdersCount,
            newOrdersCount = newOrdersCount,
            pendingOrders = pendingOrders,
            processingOrders = processingOrders,
            outOfStockItems = outOfStockItems,
            recentActivities = filteredOrders.sortedByDescending { it.createdAt }.take(5).map { order ->
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
    
    fun setTimeRange(range: String) {
        _dashboardUiState.value = _dashboardUiState.value.copy(timeRange = range)
        updateDashboardStats()
    }

    fun setCategoryFilter(category: String) {
        _foodManagementUiState.value = _foodManagementUiState.value.copy(selectedCategory = category)
        updateFilteredFoods()
    }

    fun setBranchFilter(branch: Branch?) {
        _foodManagementUiState.value = _foodManagementUiState.value.copy(selectedBranch = branch)
        updateFilteredFoods()
    }

    fun setStatusFilter(status: FoodStatus) {
        _foodManagementUiState.value = _foodManagementUiState.value.copy(selectedStatus = status)
        updateFilteredFoods()
    }

    private fun updateFilteredFoods() {
        val state = _foodManagementUiState.value
        val filtered = state.foods.filter { food ->
            val matchesCategory = state.selectedCategory == "All" || food.category == state.selectedCategory
            val matchesStatus = when (state.selectedStatus) {
                FoodStatus.ALL -> true
                FoodStatus.AVAILABLE -> food.isAvailable
                FoodStatus.OUT_OF_STOCK -> !food.isAvailable
            }
            // Branch filtering: if a branch is selected, food must be in branch.foodIds
            val matchesBranch = state.selectedBranch?.let { branch ->
                branch.foodIds.contains(food.id)
            } ?: true
            
            matchesCategory && matchesStatus && matchesBranch
        }
        _foodManagementUiState.value = _foodManagementUiState.value.copy(filteredFoods = filtered)
    }

    fun deleteFood(foodId: String) {
         viewModelScope.launch {
            foodRepository.deleteFood(foodId).onSuccess {
                // Remove foodId from any branch that contains it
                branchRepository.getBranches().onSuccess { branches ->
                    branches.forEach { branch ->
                        if (branch.foodIds.contains(foodId)) {
                             val updatedBranch = branch.copy(foodIds = branch.foodIds - foodId)
                             branchRepository.updateBranch(updatedBranch)
                        }
                    }
                }
                
                loadFoods()
                loadBranches()
            }
        }
    }
    
    fun approveOrder(orderId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, "PROCESSING")
            onSuccess()
        }
    }

    fun rejectOrder(orderId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, "REJECTED")
            onSuccess()
        }
    }

    fun startDelivery(orderId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, "DELIVERING")
            onSuccess()
        }
    }

    fun completeOrder(orderId: String) {
        viewModelScope.launch {
             orderRepository.updateOrderStatus(orderId, "COMPLETED")
        }
    }
    
    fun getOrderById(orderId: String): Order? {
        return _orders.value.find { it.id == orderId }
    }
    
    fun addPromotion(name: String, code: String, value: Int, type: Int) {
        viewModelScope.launch {
            val promo = Promotion(name = name, code = code, discountValue = value, discountType = type)
            promotionRepository.createPromotion(promo)
            loadPromotions()
        }
    }
    
    fun addTable(name: String, seats: Int) {
        viewModelScope.launch {
             tableRepository.createTable(Table(name = name, seats = seats))
             loadTables()
        }
    }
    
    fun updateTableStatus(tableId: String, status: String) {
        viewModelScope.launch {
             tableRepository.updateTableStatus(tableId, status)
             loadTables()
        }
    }
    
    fun updateTable(table: Table) {
        viewModelScope.launch {
            tableRepository.updateTable(table)
            loadTables()
        }
    }
    
    fun deleteTable(tableId: String) {
        viewModelScope.launch {
            tableRepository.deleteTable(tableId)
            loadTables()
        }
    }
    
    // Reservations
    private val _reservations = MutableStateFlow<List<TableReservation>>(emptyList())
    val reservations: StateFlow<List<TableReservation>> = _reservations.asStateFlow()
    private val reservationRepository = ReservationRepository()
    
    fun loadReservations() {
        viewModelScope.launch {
            // Load all reservations for admin
            reservationRepository.getAllReservations().onSuccess { list ->
                _reservations.value = list
            }
        }
    }
    
    fun addReservation(reservation: TableReservation) {
        viewModelScope.launch {
            reservationRepository.createReservation(reservation)
            loadReservations()
        }
    }

    // Admin reservation management functions
    fun approveReservation(reservationId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            reservationRepository.updateReservationStatus(reservationId, "CONFIRMED")
            loadReservations()
            onSuccess()
        }
    }

    fun rejectReservation(reservationId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            reservationRepository.updateReservationStatus(reservationId, "REJECTED")
            loadReservations()
            onSuccess()
        }
    }

    fun completeReservation(reservationId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            reservationRepository.updateReservationStatus(reservationId, "COMPLETED")
            loadReservations()
            onSuccess()
        }
    }

    fun getReservationById(reservationId: String): TableReservation? {
        return _reservations.value.find { it.id == reservationId }
    }
}
