package com.muatrenthenang.resfood.ui.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muatrenthenang.resfood.data.model.Topping
import com.muatrenthenang.resfood.data.repository.ToppingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ToppingManagementViewModel(
    private val repository: ToppingRepository = ToppingRepository()
) : ViewModel() {

    private val _toppings = MutableStateFlow<List<Topping>>(emptyList())
    val toppings = _toppings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        loadToppings()
    }

    fun loadToppings() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getToppings()
                .onSuccess { 
                    _toppings.value = it 
                    _error.value = null
                }
                .onFailure { 
                    _error.value = it.message 
                }
            _isLoading.value = false
        }
    }

    fun deleteTopping(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.deleteTopping(id)
                .onSuccess {
                    loadToppings() // Refresh list
                }
                .onFailure {
                    _error.value = "Failed to delete: ${it.message}"
                }
            _isLoading.value = false
        }
    }
}
