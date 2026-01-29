package com.muatrenthenang.resfood.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muatrenthenang.resfood.data.model.Food
import com.muatrenthenang.resfood.data.model.Topping
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.toMutableSet
import com.muatrenthenang.resfood.data.repository.FoodRepository
import com.muatrenthenang.resfood.data.repository.CartRepository
import com.muatrenthenang.resfood.data.repository.FavoritesRepository
import com.muatrenthenang.resfood.data.repository.OrderRepository
import com.muatrenthenang.resfood.data.repository.ToppingRepository
import com.muatrenthenang.resfood.data.repository.AuthRepository

class FoodDetailViewModel(
    private val _foodRepository: FoodRepository = FoodRepository(),
    private val _cartRepository: CartRepository = CartRepository(),
    private val _favoritesRepository: FavoritesRepository = FavoritesRepository(),
    private val _orderRepository: OrderRepository = OrderRepository(),
    private val _toppingRepository: ToppingRepository = ToppingRepository(),
    private val _authRepository: AuthRepository = AuthRepository()

) : ViewModel() {
    
    private val _food = MutableStateFlow<Food?>(null)
    val food: StateFlow<Food?> = _food.asStateFlow()

    private val _allToppings = MutableStateFlow<List<Topping>?>(null)
    val allToppings: StateFlow<List<Topping>?> = _allToppings.asStateFlow()

    private val _selectedToppings = MutableStateFlow<Set<Topping>>(emptySet())
    val selectedToppings: StateFlow<Set<Topping>> = _selectedToppings.asStateFlow()

    private val _quantity = MutableStateFlow(1)
    val quantity: StateFlow<Int> = _quantity.asStateFlow()

    private val _totalPrice = MutableStateFlow(0)
    val totalPrice: StateFlow<Int> = _totalPrice.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    // Review Logic
    private val _canReview = MutableStateFlow(false)
    val canReview: StateFlow<Boolean> = _canReview.asStateFlow()

    private val _ratingHistogram = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val ratingHistogram: StateFlow<Map<Int, Int>> = _ratingHistogram.asStateFlow()


    //

    fun loadFoodDetail(foodId: String) {
        viewModelScope.launch {
            // Load Food
            val foodResult = _foodRepository.getFood(foodId)
            if (foodResult.isSuccess) {
                val foodItem = foodResult.getOrNull()
                _food.value = foodItem
                _totalPrice.value = foodItem?.price ?: 0
                
                // Load favorites status
                foodItem?.id?.let { id ->
                    val favRes = _favoritesRepository.isFavorite(id)
                    _isFavorite.value = favRes.getOrNull() ?: false
                }
            } else {
                _food.value = null
                _isFavorite.value = false
            }

            // Load Toppings
            val toppingResult = _toppingRepository.getToppings()
            if (toppingResult.isSuccess) {
                _allToppings.value = toppingResult.getOrNull()
            } else {
                _allToppings.value = emptyList()
            }

             // Calculate histogram whenever food updates
            _food.value?.let { food ->
                calculateRatingHistogram(food.reviews)
            }
            // Check if user can review
             checkIfUserCanReview(foodId)

            updateTotalPrice()
        }
    }

    fun increaseQuantity() {
        if (_quantity.value < 10){
            _quantity.update { it + 1 }
            updateTotalPrice()
        }
    }

    fun decreaseQuantity() {
        if (_quantity.value > 1) {
            _quantity.update { it - 1 }
            updateTotalPrice()
        }
    }

    fun onToppingSelected(topping: Topping, isSelected: Boolean) {
        _selectedToppings.update { currentSelected ->
            val newSelected = currentSelected.toMutableSet()
            if (isSelected) {
                newSelected.add(topping)
            } else {
                newSelected.remove(topping)
            }
            newSelected
        }
        updateTotalPrice()
    }

    private fun updateTotalPrice() {
        val foodPrice = _food.value?.price ?: 0
        val currentQuantity = _quantity.value
        val toppingsPrice = _selectedToppings.value.sumOf { it.price }

        _totalPrice.value = (foodPrice * currentQuantity) + toppingsPrice
    }

    // One-time events (Toast messages)
    private val _toastMessage = kotlinx.coroutines.flow.MutableSharedFlow<String>(replay = 0)
    val toastMessage = _toastMessage.asSharedFlow()

    // Helper to emit toast
    private fun showToast(message: String) {
        viewModelScope.launch {
            _toastMessage.emit(message)
        }
    }

    fun addToCart(){
        viewModelScope.launch {
            val foodItem = _food.value ?: return@launch
            val result = _cartRepository.addOrUpdateCartItem(
                foodId = foodItem.id ?: return@launch,
                quantity = _quantity.value,
                note = null,
                toppings = _selectedToppings.value.toList(),
                isAccumulate = true
            )
            if (result.isSuccess) {
                showToast("Đã thêm vào giỏ hàng")
                Log.d("FoodDetailViewModel", "Added to cart successfully")
            } else {
                showToast("Thêm vào giỏ hàng thất bại: ${result.exceptionOrNull()?.message}")
                Log.d("FoodDetailViewModel", "Failed to add to cart: ${result.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    fun addToFavorites() {
        viewModelScope.launch {
            val foodItem = _food.value ?: return@launch
            val foodId = foodItem.id ?: return@launch
            if (_isFavorite.value) {
                // Currently favorite, so remove it
                val result = _favoritesRepository.removeFavorite(foodId)
                if (result.isSuccess) {
                    _isFavorite.value = false
                    showToast("Đã xóa khỏi danh sách yêu thích")
                    Log.d("FoodDetailViewModel", "Removed from favorites")
                } else {
                    showToast("Lỗi khi xóa khỏi yêu thích")
                    Log.d("FoodDetailViewModel", "Failed to remove favorite: ${result.exceptionOrNull()?.localizedMessage}")
                }
            } else {
                // Not a favorite, so add it
                val result = _favoritesRepository.addFavorite(foodId)
                if (result.isSuccess) {
                    _isFavorite.value = true
                    showToast("Đã thêm vào danh sách yêu thích")
                    Log.d("FoodDetailViewModel", "Added to favorites")
                } else {
                    showToast("Lỗi khi thêm vào yêu thích")
                    Log.d("FoodDetailViewModel", "Failed to add favorite: ${result.exceptionOrNull()?.localizedMessage}")
                }
            }
        }
    }

    private fun calculateRatingHistogram(reviews: List<com.muatrenthenang.resfood.data.model.Review>) {
        val histogram = mutableMapOf(5 to 0, 4 to 0, 3 to 0, 2 to 0, 1 to 0)
        reviews.forEach { review ->
            val star = review.star.coerceIn(1, 5)
            histogram[star] = (histogram[star] ?: 0) + 1
        }
        _ratingHistogram.value = histogram
    }

    private fun checkIfUserCanReview(foodId: String) {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            _canReview.value = false
            return
        }
        
        viewModelScope.launch {
            _orderRepository.getOrdersByUserId(currentUser.uid).collect { orders ->
                 // Check if any COMPLETED order contains this food
                 val hasOrdered = orders.any { order ->
                     order.status == "COMPLETED" && order.items.any { it.foodId == foodId }
                 }
                 _canReview.value = hasOrdered
            }
        }
    }

    // State for review submission
    private val _reviewSubmissionState = MutableStateFlow<Result<Boolean>?>(null)
    val reviewSubmissionState: StateFlow<Result<Boolean>?> = _reviewSubmissionState.asStateFlow()

    fun resetReviewSubmissionState() {
        _reviewSubmissionState.value = null
    }

    fun submitReview(comment: String, star: Int) {
         val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return
         val foodId = _food.value?.id ?: return
         
         viewModelScope.launch {
             // Lấy thông tin user mới nhất từ Firestore để có tên chính xác
             val userResult = _authRepository.getUserProfile(currentUser.uid)
             val userName = userResult.getOrNull()?.fullName ?: currentUser.displayName ?: "User"

             val review = com.muatrenthenang.resfood.data.model.Review(
                 star = star,
                 comment = comment,
                 userId = currentUser.uid,
                 userName = userName,
                 createdAt = System.currentTimeMillis()
             )

             val result = _foodRepository.addReview(foodId, review)
             _reviewSubmissionState.value = result // Update state with result
             
             if (result.isSuccess) {
                 // Refresh food data to show new review
                  loadFoodDetail(foodId)
             } else {
                 Log.e("FoodDetailViewModel", "Failed to submit review: ${result.exceptionOrNull()}")
             }
         }
    }

    suspend fun submitReviewForFood(foodId: String, review: com.muatrenthenang.resfood.data.model.Review): Result<Boolean> {
        return _foodRepository.addReview(foodId, review)
    }
}