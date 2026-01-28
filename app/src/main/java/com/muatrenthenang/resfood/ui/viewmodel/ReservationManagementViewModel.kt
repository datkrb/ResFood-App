package com.muatrenthenang.resfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muatrenthenang.resfood.data.model.TableReservation
import com.muatrenthenang.resfood.data.repository.AuthRepository
import com.muatrenthenang.resfood.data.repository.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ReservationUiState {
    object Loading : ReservationUiState()
    data class Success(
        val reservations: List<TableReservation>,
        val counts: Map<String, Int>
    ) : ReservationUiState()
    data class Error(val message: String) : ReservationUiState()
}

class ReservationManagementViewModel : ViewModel() {
    private val repository = ReservationRepository()
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow<ReservationUiState>(ReservationUiState.Loading)
    val uiState: StateFlow<ReservationUiState> = _uiState.asStateFlow()
    
    // Cache for filtering locally to avoid excessive API calls
    private var allReservations: List<TableReservation> = emptyList()

    init {
        loadReservations()
    }

    fun loadReservations() {
        val userId = authRepository.getCurrentUserId() ?: return
        
        viewModelScope.launch {
            _uiState.value = ReservationUiState.Loading
            
            repository.getReservationsByUser(userId)
                .onSuccess { list ->
                    // Sort by newest first
                    allReservations = list.sortedByDescending { it.createdAt }
                    updateUiState()
                }
                .onFailure {
                    _uiState.value = ReservationUiState.Error(it.message ?: "Lỗi tải đơn đặt bàn")
                }
        }
    }
    
    private fun updateUiState() {
        val counts = mapOf(
            "PENDING" to allReservations.count { it.status == "PENDING" },
            "CONFIRMED" to allReservations.count { it.status == "CONFIRMED" },
            "COMPLETED" to allReservations.count { it.status == "COMPLETED" },
            "CANCELLED" to allReservations.count { it.status == "CANCELLED" }
        )
        
        _uiState.value = ReservationUiState.Success(
            reservations = allReservations,
            counts = counts
        )
    }
    
    fun getFilteredReservations(status: String = "ALL"): List<TableReservation> {
        return if (status == "ALL") {
            allReservations
        } else {
            allReservations.filter { it.status == status }
        }
    }

    fun cancelReservation(id: String) {
        viewModelScope.launch {
            repository.cancelReservation(id)
                .onSuccess {
                    // Refresh data
                    loadReservations()
                }
                .onFailure {
                    // Ideally show toast/snackbar, but for now we rely on UI state error or just refresh
                    loadReservations() 
                }
        }
    }
}
