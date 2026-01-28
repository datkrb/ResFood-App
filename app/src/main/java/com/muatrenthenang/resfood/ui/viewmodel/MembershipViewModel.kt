package com.muatrenthenang.resfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.muatrenthenang.resfood.data.model.Rank
import com.muatrenthenang.resfood.data.model.RankReward
import com.muatrenthenang.resfood.data.repository.RankRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MembershipViewModel : ViewModel() {
    private val rankRepository = RankRepository()
    private val auth = FirebaseAuth.getInstance()
    
    private val _currentRank = MutableStateFlow(Rank.MEMBER)
    val currentRank: StateFlow<Rank> = _currentRank.asStateFlow()
    
    private val _totalSpending = MutableStateFlow(0.0)
    val totalSpending: StateFlow<Double> = _totalSpending.asStateFlow()
    
    private val _uiState = MutableStateFlow<MembershipUiState>(MembershipUiState.Loading)
    val uiState: StateFlow<MembershipUiState> = _uiState.asStateFlow()
    
    private val _claimedRewards = MutableStateFlow<List<String>>(emptyList())
    
    // Derived state for available rewards with status
    private val _rewardsList = MutableStateFlow<List<RankRewardItem>>(emptyList())
    val rewardsList: StateFlow<List<RankRewardItem>> = _rewardsList.asStateFlow()

    init {
        loadData()
    }
    
    fun loadData() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.value = MembershipUiState.Error("User not logged in")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = MembershipUiState.Loading
            rankRepository.getRankInfo(userId)
                .onSuccess { (rank, spending) ->
                    _currentRank.value = rank
                    _totalSpending.value = spending
                    loadClaimedRewards(userId)
                    _uiState.value = MembershipUiState.Success
                }
                .onFailure {
                    _uiState.value = MembershipUiState.Error(it.message ?: "Failed to load rank")
                }
        }
    }
    
    private fun loadClaimedRewards(userId: String) {
        viewModelScope.launch {
            // In a real app we might fetch this from user object or separate query
            // For now assuming we have it on user object sync or similar.
            // Let's refactor RankRepository.getRankInfo to return full User or separate call? 
            // We can just fetch user details.
            
            // Simplified: Assuming we can get it via user repo or just use what we have.
            // Let's try to query just that field or re-use existing VM pattern.
            // For expediency, I will just re-fetch user document in repo if needed
            // Actually RankRepository.claimReward updates it.
            
            // Let's fetch the list (mocking sync or add method to repo)
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            try {
                val doc = db.collection("users").document(userId).get().await()
                @Suppress("UNCHECKED_CAST")
                val claimed = doc.get("claimedRewards") as? List<String> ?: emptyList()
                _claimedRewards.value = claimed
                updateRewardsList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun updateRewardsList() {
        val current = _currentRank.value
        val allRewards = rankRepository.availableRewards
        val claimed = _claimedRewards.value
        
        _rewardsList.value = allRewards.map { reward ->
            val status = when {
                claimed.contains(reward.id) -> RewardStatus.CLAIMED
                current.threshold >= reward.rankRequired.threshold -> RewardStatus.AVAILABLE
                else -> RewardStatus.LOCKED
            }
            RankRewardItem(reward, status)
        }
    }
    
    fun claimReward(reward: RankReward) {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            // Update UI optimistically or show loading
            rankRepository.claimReward(userId, reward)
                .onSuccess {
                    // Update locally
                    val newClaimed = _claimedRewards.value.toMutableList().apply { add(reward.id) }
                    _claimedRewards.value = newClaimed
                    updateRewardsList()
                }
                .onFailure {
                    // Show error snackbar? (Handled by UI observing error state or one-shot event)
                }
        }
    }
    
    // Helper for Progress
    fun getNextRankTarget(): Double {
        val current = _currentRank.value
        val next = Rank.getNextRank(current)
        return next?.threshold ?: current.threshold
    }
    
    fun getProgress(): Float {
        val spending = _totalSpending.value
        val current = _currentRank.value
        val next = Rank.getNextRank(current)
        
        if (next == null) return 1f // Max rank
        
        // Linear progress from current threshold to next threshold? 
        // Or absolute from 0? Usually absolute 0 to next target looks better for "Total Spending"
        // Let's do absolute % of next target.
        return (spending / next.threshold).toFloat().coerceIn(0f, 1f)
    }
}

sealed class MembershipUiState {
    object Loading : MembershipUiState()
    object Success : MembershipUiState()
    data class Error(val message: String) : MembershipUiState()
}

data class RankRewardItem(
    val reward: RankReward,
    val status: RewardStatus
)

enum class RewardStatus {
    LOCKED, AVAILABLE, CLAIMED
}
