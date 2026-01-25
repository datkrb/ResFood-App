package com.muatrenthenang.resfood.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muatrenthenang.resfood.data.model.Food
import com.muatrenthenang.resfood.data.model.Topping
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.toMutableSet
import com.muatrenthenang.resfood.data.repository.FoodRepository
import com.muatrenthenang.resfood.data.repository.CartRepository
import com.muatrenthenang.resfood.data.repository.FavoritesRepository

class FoodDetailViewModel(
    private val _foodRepository: FoodRepository = FoodRepository(),
    private val _cartRepository: CartRepository = CartRepository(),
    private val _favoritesRepository: FavoritesRepository = FavoritesRepository()

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

    val listTopping = listOf(
        Topping(
            name = "Trứng lòng đào",
            price = 5000,
            imageUrl = "https://cdn.tgdd.vn/2021/11/CookRecipe/GalleryStep/thanh-pham-453.jpg"
        ),
        Topping(
            name = "Thịt nướng",
            price = 7000,
            imageUrl = "https://cdn.tgdd.vn/2021/11/CookRecipe/GalleryStep/thanh-pham-453.jpg"
        ),
        Topping(
            name = "Tốp mỡ",
            price = 3000,
            imageUrl = "https://cdn.tgdd.vn/2021/11/CookRecipe/GalleryStep/thanh-pham-453.jpg"
        ),
    )
    //

    fun loadFoodDetail(foodId: String) {
        viewModelScope.launch {
            val result = _foodRepository.getFood(foodId)
            if (result.isSuccess) {
                val foodItem = result.getOrNull()
                _food.value = foodItem
                _totalPrice.value = foodItem?.price ?: 0
                // Check favorites status for this food
                foodItem?.id?.let { id ->
                    val favRes = _favoritesRepository.isFavorite(id)
                    if (favRes.isSuccess) {
                        _isFavorite.value = favRes.getOrNull() ?: false
                    } else {
                        _isFavorite.value = false
                    }
                }
            } else {
                _food.value = null
                _isFavorite.value = false
            }
        }
        //_food.value = sampleFoods.find { it.id == foodId }
        _allToppings.value = listTopping

        updateTotalPrice()
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

    fun addToCart(){
        viewModelScope.launch {
            val foodItem = _food.value ?: return@launch
            val result = _cartRepository.addOrUpdateCartItem(
                foodId = foodItem.id ?: return@launch,
                quantity = _quantity.value,
                note = null
            )
            if (result.isSuccess) {
                Log.d("FoodDetailViewModel", "Added to cart successfully")
            } else {
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
                    Log.d("FoodDetailViewModel", "Removed from favorites")
                } else {
                    Log.d("FoodDetailViewModel", "Failed to remove favorite: ${result.exceptionOrNull()?.localizedMessage}")
                }
            } else {
                // Not a favorite, so add it
                val result = _favoritesRepository.addFavorite(foodId)
                if (result.isSuccess) {
                    _isFavorite.value = true
                    Log.d("FoodDetailViewModel", "Added to favorites")
                } else {
                    Log.d("FoodDetailViewModel", "Failed to add favorite: ${result.exceptionOrNull()?.localizedMessage}")
                }
            }
        }
    }

}