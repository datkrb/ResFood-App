package com.muatrenthenang.resfood.ui.screens.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muatrenthenang.resfood.data.local.MockReviewStorage
import com.muatrenthenang.resfood.data.model.Food
import com.muatrenthenang.resfood.data.model.Review
import com.muatrenthenang.resfood.data.repository.AuthRepository
import com.muatrenthenang.resfood.data.repository.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class ReviewViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val foodRepository = FoodRepository()

    private val _rating = MutableStateFlow(0)
    val rating: StateFlow<Int> = _rating.asStateFlow()

    private val _comment = MutableStateFlow("")
    val comment: StateFlow<String> = _comment.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _food = MutableStateFlow<Food?>(null)
    val food: StateFlow<Food?> = _food.asStateFlow()

    private var foodId: String = ""

    fun setFoodId(id: String) {
        foodId = id
        loadFood()
    }

    private fun loadFood() {
        if (foodId.isEmpty()) return
        viewModelScope.launch {
            val result = foodRepository.getFood(foodId)
            result.onSuccess { food ->
                _food.value = food
            }.onFailure {
                _food.value = null
            }
        }
    }

    fun setRating(value: Int) {
        _rating.value = value
        _errorMessage.value = null
    }

    fun setComment(value: String) {
        _comment.value = value
        _errorMessage.value = null
    }

    fun submitReview(onSuccess: () -> Unit) {
        if (foodId.isEmpty()) {
            _errorMessage.value = "Lỗi: Không tìm thấy món ăn"
            return
        }

        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            _errorMessage.value = "Vui lòng đăng nhập để đánh giá"
            return
        }

        if (_rating.value == 0) {
            _errorMessage.value = "Vui lòng chọn số sao đánh giá"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            // Simulate delay để giống như đang gọi API
            delay(500)

            // Get user details for name (vẫn lấy từ Firebase để có tên thật)
            val userName = try {
                val userResult = authRepository.getUserDetails(userId)
                userResult.getOrNull()?.fullName ?: "Người dùng"
            } catch (e: Exception) {
                "Người dùng"
            }

            // Tạo review object
            val review = Review(
                star = _rating.value,
                comment = _comment.value,
                userId = userId,
                userName = userName,
                createdAt = System.currentTimeMillis()
            )

            MockReviewStorage.addReview(foodId, review)

            _isLoading.value = false

            // Reset form
            _rating.value = 0
            _comment.value = ""

            // Call success callback
            onSuccess()
        }
    }
}

