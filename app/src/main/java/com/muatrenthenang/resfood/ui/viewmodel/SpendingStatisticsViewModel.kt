package com.muatrenthenang.resfood.ui.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.muatrenthenang.resfood.data.model.Order
import com.muatrenthenang.resfood.data.repository.AuthRepository
import com.muatrenthenang.resfood.data.repository.FoodRepository
import com.muatrenthenang.resfood.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel quản lý thống kê chi tiêu của người dùng
 */
class SpendingStatisticsViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository()
    private val orderRepository = OrderRepository()
    private val foodRepository = FoodRepository()

    // Tổng chi tiêu trong khoảng thời gian
    private val _totalSpending = MutableStateFlow(0L)
    val totalSpending: StateFlow<Long> = _totalSpending.asStateFlow()

    // Chi tiêu theo danh mục
    private val _categorySpending = MutableStateFlow<List<CategorySpending>>(emptyList())
    val categorySpending: StateFlow<List<CategorySpending>> = _categorySpending.asStateFlow()

    // Khoảng thời gian đang chọn (Tuần/Tháng)
    private val _selectedPeriod = MutableStateFlow(SpendingPeriod.WEEK)
    val selectedPeriod: StateFlow<SpendingPeriod> = _selectedPeriod.asStateFlow()

    // Dữ liệu chi tiêu theo tuần (7 ngày)
    private val _weeklyData = MutableStateFlow<List<Pair<String, Long>>>(emptyList())
    val weeklyData: StateFlow<List<Pair<String, Long>>> = _weeklyData.asStateFlow()

    // Dữ liệu chi tiêu theo tháng (30 ngày, nhóm theo tuần)
    private val _monthlyData = MutableStateFlow<List<Pair<String, Long>>>(emptyList())
    val monthlyData: StateFlow<List<Pair<String, Long>>> = _monthlyData.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Cache foods for category lookup
    private var foodCategoryMap: Map<String, String> = emptyMap()
    
    // Cache orders to avoid losing data when switching periods
    private var cachedOrders: List<Order> = emptyList()

    init {
        loadData()
    }

    fun setSelectedPeriod(period: SpendingPeriod) {
        _selectedPeriod.value = period
        // Use cached orders instead of empty list
        calculateStatistics(cachedOrders)
    }

    private fun loadData() {
        val userId = authRepository.getCurrentUserId()
        android.util.Log.d("SpendingStatistics", "Loading data for user: $userId")
        
        if (userId == null) {
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            
            // 1. Load foods FIRST (wait for completion before processing orders)
            android.util.Log.d("SpendingStatistics", "Start fetching foods...")
            val foodsResult = foodRepository.getFoods()
            foodsResult.onSuccess { foods ->
                android.util.Log.d("SpendingStatistics", "Fetched ${foods.size} foods")
                foodCategoryMap = foods.associate { it.id to it.category }
            }.onFailure {
                android.util.Log.e("SpendingStatistics", "Failed to fetch foods", it)
            }

            // 2. Now start observing orders (foods are already loaded)
            android.util.Log.d("SpendingStatistics", "Start observing orders...")
            try {
                orderRepository.getOrdersByUserId(userId).collect { orders ->
                    android.util.Log.d("SpendingStatistics", "Received ${orders.size} orders")
                    processOrders(orders)
                    
                    // Turn off loading after first orders received
                    if (_isLoading.value) {
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("SpendingStatistics", "Error observing orders", e)
                _isLoading.value = false
            }
        }
    }

    private fun processOrders(orders: List<Order>) {
        viewModelScope.launch {
            // Filter: tính tất cả đơn hàng trừ CANCELLED và REJECTED
            // Bao gồm: PENDING, PROCESSING, DELIVERING, COMPLETED
            val validOrders = orders.filter { 
                it.status != "CANCELLED" && it.status != "REJECTED" 
            }
            android.util.Log.d("SpendingStatistics", "Processing ${validOrders.size} valid orders (excluding cancelled/rejected)")

            // Cache the orders for period switching
            cachedOrders = validOrders
            
            // Calculate statistics based on selected period
            calculateStatistics(validOrders)
        }
    }

    private fun calculateStatistics(orders: List<Order>? = null) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch

            // If orders not provided, we need to fetch them
            val allOrders = orders ?: run {
                // This is a simplified approach - in real app, you might want to cache orders
                emptyList()
            }

            // Orders are already filtered in processOrders (excluding CANCELLED/REJECTED)

            val calendar = Calendar.getInstance()
            val now = calendar.time

            // Calculate date boundaries
            val (startDate, endDate) = when (_selectedPeriod.value) {
                SpendingPeriod.WEEK -> {
                    calendar.add(Calendar.DAY_OF_YEAR, -7)
                    Pair(calendar.time, now)
                }
                SpendingPeriod.MONTH -> {
                    calendar.time = now
                    calendar.add(Calendar.DAY_OF_YEAR, -30)
                    Pair(calendar.time, now)
                }
            }

            // Filter orders within the period (inclusive of boundaries)
            val periodOrders = allOrders.filter { order ->
                val orderDate = order.createdAt.toDate()
                // Use !before for >= startDate and !after for <= endDate
                !orderDate.before(startDate) && !orderDate.after(endDate)
            }

            // Calculate total spending
            _totalSpending.value = periodOrders.sumOf { it.total.toLong() }

            // Calculate category spending
            calculateCategorySpending(periodOrders)

            // Calculate time-based data
            when (_selectedPeriod.value) {
                SpendingPeriod.WEEK -> calculateWeeklyData(periodOrders)
                SpendingPeriod.MONTH -> calculateMonthlyData(periodOrders)
            }
        }
    }

    private fun calculateCategorySpending(orders: List<Order>) {
        // Group items by category
        val categoryAmounts = mutableMapOf<String, Long>()
        val categoryItemCounts = mutableMapOf<String, Int>() // Track total items (món) per category

        orders.forEach { order ->
            order.items.forEach { item ->
                val category = foodCategoryMap[item.foodId] ?: "Khác"
                val itemTotal = (item.price * item.quantity).toLong()

                categoryAmounts[category] = (categoryAmounts[category] ?: 0L) + itemTotal
                
                // Count items (món), not orders
                categoryItemCounts[category] = (categoryItemCounts[category] ?: 0) + item.quantity
            }
        }

        val total = categoryAmounts.values.sum()

        val categoryList = categoryAmounts.map { (category, amount) ->
            CategorySpending(
                name = category,
                displayName = getCategoryDisplayName(category),
                amount = amount,
                percentage = if (total > 0) (amount.toFloat() / total.toFloat()) * 100f else 0f,
                orderCount = categoryItemCounts[category] ?: 0, // This is now item count (món)
                color = getCategoryColor(category)
            )
        }.sortedByDescending { it.amount }

        _categorySpending.value = categoryList
    }

    private fun calculateWeeklyData(orders: List<Order>) {
        val dayFormatter = SimpleDateFormat("EEE", Locale("vi", "VN"))
        val calendar = Calendar.getInstance()

        // Create last 7 days
        val days = mutableListOf<Pair<String, Long>>()

        for (i in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)

            val dayStart = calendar.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.time

            val dayEnd = calendar.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.time

            val dayTotal = orders.filter { order ->
                val orderDate = order.createdAt.toDate()
                orderDate.after(dayStart) && orderDate.before(dayEnd)
            }.sumOf { it.total.toLong() }

            val dayLabel = dayFormatter.format(dayStart)
            days.add(Pair(dayLabel, dayTotal))
        }

        _weeklyData.value = days
    }

    private fun calculateMonthlyData(orders: List<Order>) {
        val calendar = Calendar.getInstance()

        // Group by week of month (last 4 weeks)
        val weeks = mutableListOf<Pair<String, Long>>()

        for (i in 3 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.WEEK_OF_YEAR, -i)

            val weekStart = calendar.apply {
                set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.time

            calendar.add(Calendar.DAY_OF_WEEK, 6)
            val weekEnd = calendar.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.time

            val weekTotal = orders.filter { order ->
                val orderDate = order.createdAt.toDate()
                orderDate.after(weekStart) && orderDate.before(weekEnd)
            }.sumOf { it.total.toLong() }

            val weekLabel = "Tuần ${4 - i}"
            weeks.add(Pair(weekLabel, weekTotal))
        }

        _monthlyData.value = weeks
    }

    private fun getCategoryDisplayName(category: String): String {
        return when (category.lowercase()) {
            "main", "main_course", "món chính" -> "Món chính"
            "drink", "drinks", "beverage", "nước uống" -> "Nước uống"
            "dessert", "desserts", "tráng miệng" -> "Tráng miệng"
            "appetizer", "appetizers", "khai vị" -> "Khai vị"
            else -> category.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
    }

    private fun getCategoryColor(category: String): Color {
        return when (category.lowercase()) {
            "main", "main_course", "món chính" -> Color(0xFFE53935) // Red
            "drink", "drinks", "beverage", "nước uống" -> Color(0xFF1E88E5) // Blue
            "dessert", "desserts", "tráng miệng" -> Color(0xFFFF9800) // Orange
            "appetizer", "appetizers", "khai vị" -> Color(0xFF43A047) // Green
            else -> Color(0xFF8E24AA) // Purple
        }
    }
}

/**
 * Enum cho khoảng thời gian thống kê
 */
enum class SpendingPeriod {
    WEEK,
    MONTH
}

/**
 * Data class cho chi tiêu theo danh mục
 */
data class CategorySpending(
    val name: String,
    val displayName: String,
    val amount: Long,
    val percentage: Float,
    val orderCount: Int,
    val color: Color
)
