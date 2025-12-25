package com.muatrenthenang.resfood.ui.screens.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muatrenthenang.resfood.data.model.Food
import com.muatrenthenang.resfood.data.model.Review
import com.muatrenthenang.resfood.data.repository.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReviewListViewModel : ViewModel() {
    private val foodRepository = FoodRepository()

    private val _food = MutableStateFlow<Food?>(null)
    val food: StateFlow<Food?> = _food.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _ratingStats = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val ratingStats: StateFlow<Map<Int, Int>> = _ratingStats.asStateFlow()

    private val _averageRating = MutableStateFlow(0f)
    val averageRating: StateFlow<Float> = _averageRating.asStateFlow()

    fun loadData(foodId: String) {
        viewModelScope.launch {
            // Load food info
            val foodResult = foodRepository.getFood(foodId)
            foodResult.onSuccess { food ->
                _food.value = food

                // Use reviews from Food model
                val allReviews = food.reviews
                _reviews.value = allReviews.sortedByDescending { it.createdAt }

                // Calculate rating statistics
                val stats = mutableMapOf<Int, Int>()
                for (star in 1..5) {
                    stats[star] = allReviews.count { it.star == star }
                }
                _ratingStats.value = stats

                // Average rating from food (already precomputed in repo) or fallback to calc
                _averageRating.value = if (food.reviews.isEmpty()) 0f else food.rating
            }.onFailure {
                _food.value = null
                _reviews.value = emptyList()
                _ratingStats.value = emptyMap()
                _averageRating.value = 0f
            }
        }
    }
}

