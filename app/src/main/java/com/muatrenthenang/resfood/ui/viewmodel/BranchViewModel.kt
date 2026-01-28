package com.muatrenthenang.resfood.ui.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muatrenthenang.resfood.data.model.Address
import com.muatrenthenang.resfood.data.model.Branch
import com.muatrenthenang.resfood.data.repository.BranchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Branch Management Screen
 * Manages single branch data following MVVM pattern
 */
class BranchViewModel : ViewModel() {

    private val branchRepository = BranchRepository()

    // UI State
    private val _uiState = MutableStateFlow(BranchUiState())
    val uiState: StateFlow<BranchUiState> = _uiState.asStateFlow()

    // Form State
    private val _formState = MutableStateFlow(BranchFormState())
    val formState: StateFlow<BranchFormState> = _formState.asStateFlow()

    // Action result
    private val _actionResult = MutableStateFlow<String?>(null)
    val actionResult: StateFlow<String?> = _actionResult.asStateFlow()

    init {
        loadBranch()
    }

    /**
     * Load primary branch from Firestore
     */
   fun loadBranch() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            branchRepository.getPrimaryBranch().onSuccess { branch ->
                _formState.value = BranchFormState(
                    name = branch.name,
                    phone = branch.phone,
                    openingHours = branch.openingHours,
                    maxCapacity = branch.maxCapacity.toString(),
                    tableCount = branch.tableCount.toString(),
                    addressLine = branch.address.addressLine,
                    ward = branch.address.ward,
                    district = branch.address.district,
                    city = branch.address.city,
                    latitude = branch.address.latitude,
                    longitude = branch.address.longitude
                )
            }.onFailure { error ->
                _actionResult.value = "Lỗi tải thông tin: ${error.message}"
            }
            
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    /**
     * Update form field
     */
    fun updateFormField(
        name: String? = null,
        phone: String? = null,
        openingHours: String? = null,
        maxCapacity: String? = null,
        tableCount: String? = null,
        addressLine: String? = null,
        ward: String? = null,
        district: String? = null,
        city: String? = null
    ) {
        _formState.value = _formState.value.copy(
            name = name ?: _formState.value.name,
            phone = phone ?: _formState.value.phone,
            openingHours = openingHours ?: _formState.value.openingHours,
            maxCapacity = maxCapacity ?: _formState.value.maxCapacity,
            tableCount = tableCount ?: _formState.value.tableCount,
            addressLine = addressLine ?: _formState.value.addressLine,
            ward = ward ?: _formState.value.ward,
            district = district ?: _formState.value.district,
            city = city ?: _formState.value.city
        )
    }

    /**
     * Update location coordinates (from GPS or Map Picker)
     */
    fun updateLocation(lat: Double, lng: Double) {
        _formState.value = _formState.value.copy(
            latitude = lat,
            longitude = lng
        )
    }

    /**
     * Save branch to Firestore
     */
    fun saveBranch(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            
            val form = _formState.value
            val updatedBranch = Branch(
                id = Branch.PRIMARY_BRANCH_ID,
                name = form.name,
                phone = form.phone,
                openingHours = form.openingHours,
                maxCapacity = form.maxCapacity.toIntOrNull() ?: 50,
                tableCount = form.tableCount.toIntOrNull() ?: 10,
                address = Address(
                    id = "restaurant_address",
                    label = "Nhà hàng",
                    addressLine = form.addressLine,
                    ward = form.ward,
                    district = form.district,
                    city = form.city,
                    contactName = form.name,
                    phone = form.phone,
                    isDefault = true,
                    latitude = form.latitude,
                    longitude = form.longitude
                )
            )
            
            branchRepository.updateBranch(updatedBranch).onSuccess {
                _actionResult.value = "Lưu thông tin thành công!"
                onSuccess()
            }.onFailure { error ->
                _actionResult.value = "Lỗi: ${error.message}"
            }
            
            _uiState.value = _uiState.value.copy(isSaving = false)
        }
    }

    fun clearResult() {
        _actionResult.value = null
    }
}

/**
 * UI State for Branch Management Screen
 */
data class BranchUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false
)

/**
 * Form State for Branch editing
 */
data class BranchFormState(
    val name: String = "",
    val phone: String = "",
    val openingHours: String = "",
    val maxCapacity: String = "",
    val tableCount: String = "",
    val addressLine: String = "",
    val ward: String = "",
    val district: String = "",
    val city: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)
