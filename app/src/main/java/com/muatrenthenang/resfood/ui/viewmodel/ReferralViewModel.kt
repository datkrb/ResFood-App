package com.muatrenthenang.resfood.ui.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.muatrenthenang.resfood.data.model.User
import com.muatrenthenang.resfood.data.repository.ReferralRepository
import com.muatrenthenang.resfood.data.repository.UserRepository
import com.muatrenthenang.resfood.R
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReferralUiState(
    val referralCode: String = "",
    val referredUsers: List<ReferredUserInfo> = emptyList(),
    val totalReferred: Int = 0,
    val canEnterCode: Boolean = false,
    val remainingHours: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

data class ReferredUserInfo(
    val id: String,
    val name: String,
    val avatarUrl: String?
)

data class ReferralStep(
    val step: String,
    val titleResId: Int,
    val descriptionResId: Int
)

data class ReferralHistoryItem(
    val name: String,
    val date: String,
    val status: String,
    val reward: String
)

class ReferralViewModel(application: Application) : AndroidViewModel(application) {
    
    private val referralRepository = ReferralRepository()
    private val userRepository = UserRepository()
    private val auth = FirebaseAuth.getInstance()
    
    private val _uiState = MutableStateFlow(ReferralUiState())
    val uiState: StateFlow<ReferralUiState> = _uiState.asStateFlow()
    
    private val _inputCode = MutableStateFlow("")
    val inputCode: StateFlow<String> = _inputCode.asStateFlow()
    
    private val _referralSteps = MutableStateFlow<List<ReferralStep>>(emptyList())
    val referralSteps: StateFlow<List<ReferralStep>> = _referralSteps.asStateFlow()
    
    // Legacy properties for ReferralHistoryScreen
    private val _referralHistory = MutableStateFlow<List<ReferralHistoryItem>>(emptyList())
    val referralHistory: StateFlow<List<ReferralHistoryItem>> = _referralHistory.asStateFlow()
    
    private val _totalInvites = MutableStateFlow(0)
    val totalInvites: StateFlow<Int> = _totalInvites.asStateFlow()
    
    private val _totalReward = MutableStateFlow("0đ")
    val totalReward: StateFlow<String> = _totalReward.asStateFlow()
    
    init {
        loadReferralData()
        loadReferralSteps()
    }
    
    private fun loadReferralSteps() {
        _referralSteps.value = listOf(
            ReferralStep("1", R.string.referral_step1_title, R.string.referral_step1_desc),
            ReferralStep("2", R.string.referral_step2_title, R.string.referral_step2_desc),
            ReferralStep("3", R.string.referral_step3_title, R.string.referral_step3_desc)
        )
    }
    
    fun loadReferralData() {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Lấy thông tin user hiện tại
                val userResult = userRepository.getCurrentUser()
                val user = userResult.getOrNull()
                
                if (user != null) {
                    // Nếu chưa có referralCode thì tạo
                    val code = if (user.referralCode.isBlank()) {
                        referralRepository.initReferralCode(userId).getOrNull() 
                            ?: User.generateReferralCode(userId)
                    } else {
                        user.referralCode
                    }
                    
                    // Lấy danh sách người đã mời
                    val referredResult = referralRepository.getReferredUsers(userId)
                    val referredUsers = referredResult.getOrNull()?.map { u ->
                        ReferredUserInfo(
                            id = u.id,
                            name = u.fullName,
                            avatarUrl = u.avatarUrl
                        )
                    } ?: emptyList()
                    
                    _uiState.value = _uiState.value.copy(
                        referralCode = code,
                        referredUsers = referredUsers,
                        totalReferred = referredUsers.size,
                        canEnterCode = user.canUseReferralCode(),
                        remainingHours = user.getRemainingHoursForReferral(),
                        isLoading = false
                    )
                    
                    // Populate history for ReferralHistoryScreen
                    _referralHistory.value = referredUsers.map { 
                        ReferralHistoryItem(
                            name = it.name,
                            date = getApplication<Application>().getString(R.string.me_status_completed),
                            status = getApplication<Application>().getString(R.string.me_status_completed),
                            reward = "+50.000đ"
                        )
                    }
                    _totalInvites.value = referredUsers.size
                    _totalReward.value = "${referredUsers.size * 50000}đ"
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Không thể tải thông tin người dùng"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Đã có lỗi xảy ra"
                )
            }
        }
    }
    
    fun updateInputCode(code: String) {
        _inputCode.value = code.uppercase().take(10)
    }
    
    fun applyReferralCode() {
        val code = _inputCode.value.trim()
        if (code.length != 10) {
            _uiState.value = _uiState.value.copy(error = "Mã giới thiệu phải có 10 ký tự")
            return
        }
        
        val currentUserId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Tìm người có mã này
            val referrerResult = referralRepository.findUserByReferralCode(code)
            val referrer = referrerResult.getOrNull()
            
            if (referrer == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Mã giới thiệu không tồn tại"
                )
                return@launch
            }
            
            // Không thể tự mời chính mình
            if (referrer.id == currentUserId) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Bạn không thể sử dụng mã của chính mình"
                )
                return@launch
            }
            
            // Áp dụng mã
            val applyResult = referralRepository.applyReferralCode(referrer.id, currentUserId)
            
            if (applyResult.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    canEnterCode = false,
                    successMessage = getApplication<Application>().getString(R.string.referral_msg_apply_success, referrer.fullName)
                )
                _inputCode.value = ""
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = applyResult.exceptionOrNull()?.message ?: "Không thể áp dụng mã"
                )
            }
        }
    }
    
    fun copyReferralCode(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Referral Code", _uiState.value.referralCode)
        clipboard.setPrimaryClip(clip)
        _uiState.value = _uiState.value.copy(successMessage = getApplication<Application>().getString(R.string.referral_msg_copy_success))
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}
