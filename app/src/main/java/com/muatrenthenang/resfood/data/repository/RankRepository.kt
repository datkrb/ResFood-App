package com.muatrenthenang.resfood.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.muatrenthenang.resfood.data.model.Rank
import com.muatrenthenang.resfood.data.model.RankReward
import com.muatrenthenang.resfood.data.model.RankRewardType
import kotlinx.coroutines.tasks.await

class RankRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    // Define rewards directly here or fetch from Config if dynamic
    val availableRewards = listOf(
        // Silver Rewards
        RankReward("SILVER_50K_SHIP", "Voucher Free Ship 50k", "Giảm 50k phí ship cho đơn từ 100k", Rank.SILVER, 50000, RankRewardType.FREE_SHIP),
        RankReward("SILVER_100K", "Voucher 100k", "Giảm 100k cho đơn từ 200k", Rank.SILVER, 100000),
        
        // Gold Rewards
        RankReward("GOLD_150K", "Voucher 150k", "Giảm 150k cho đơn từ 500k", Rank.GOLD, 150000),
        RankReward("GOLD_FREE_SHIP_UNLIMITED", "Free Ship Đặc Biệt", "Giảm tối nđa 100k phí ship", Rank.GOLD, 100000, RankRewardType.FREE_SHIP),
        
        // Diamond Rewards
        RankReward("DIAMOND_300K", "Voucher 300k", "Giảm 300k cho đơn từ 1 triệu", Rank.DIAMOND, 300000),
        RankReward("DIAMOND_BIRTHDAY", "Quà sinh nhật", "Phần quà đặc biệt tháng sinh nhật", Rank.DIAMOND, 0, RankRewardType.GIFT)
    )
    
    suspend fun getRankInfo(userId: String): Result<Pair<Rank, Double>> {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            val spending = doc.getDouble("totalSpending") ?: 0.0
            val rank = Rank.getRankFromSpending(spending)
            
            // Validate if rank field in DB matches calculation, if not update it (optional sync)
            val currentDbRank = doc.getString("rank")
            if (currentDbRank != rank.displayName) {
                db.collection("users").document(userId).update("rank", rank.displayName)
            }
            
            Result.success(Pair(rank, spending))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Claim a specific reward.
     * 1. Add reward ID to user's `claimedRewards` list.
     * 2. PROMOTION CREATION: Create a real 'Promotion' document in 'promotions' collection.
     *    This ensures the voucher appears in the user's "My Vouchers" list and works with checkout.
     */
    suspend fun claimReward(userId: String, reward: RankReward): Result<Boolean> {
        return try {
            val userRef = db.collection("users").document(userId)
            val promotionsRef = db.collection("promotions")
            
            // Run transaction to ensure atomicity
            db.runTransaction { transaction ->  
                val snapshot = transaction.get(userRef)
                
                // 1. Check if already claimed
               @Suppress("UNCHECKED_CAST")
                val claimedList = snapshot.get("claimedRewards") as? List<String> ?: emptyList()
                if (claimedList.contains(reward.id)) {
                    throw Exception("Bạn đã nhận phần thưởng này rồi")
                }
                
                // 2. Check if user has sufficient rank
                val spending = snapshot.getDouble("totalSpending") ?: 0.0
                val rank = Rank.getRankFromSpending(spending)
                if (rank.threshold < reward.rankRequired.threshold) {
                     throw Exception("Bạn chưa đạt hạng ${reward.rankRequired.displayName} để nhận thưởng")
                }
                
                // 3. Mark as claimed in User profile
                transaction.update(userRef, "claimedRewards", FieldValue.arrayUnion(reward.id))
                
                // 4. Create the Promotion (Voucher)
                // Generate a unique code: e.g., RANK_SILVER_ABC123
                val uniqueSuffix = java.util.UUID.randomUUID().toString().take(6).uppercase()
                val voucherCode = "RANK_${reward.id}_$uniqueSuffix" // e.g., RANK_SILVER_50K_SHIP_A1B2C3
                
                val now = com.google.firebase.Timestamp.now()
                val thirtyDaysInSeconds = 30L * 24 * 60 * 60
                val expiryDate = com.google.firebase.Timestamp(now.seconds + thirtyDaysInSeconds, now.nanoseconds)
                
                // Map RankRewardType to Promotion fields
                val (applyFor, discountType) = when (reward.type) {
                    RankRewardType.FREE_SHIP -> Pair("SHIP", 1) // 1 = Amount
                    RankRewardType.VOUCHER -> Pair("ALL", 1)    // 1 = Amount
                    RankRewardType.GIFT -> Pair("ALL", 1)       // Treat Gift as a voucher for now? Or handle differently.
                }
                
                val newPromotionRef = promotionsRef.document()
                val newPromotion = com.muatrenthenang.resfood.data.model.Promotion(
                    id = newPromotionRef.id,
                    name = reward.title,
                    code = voucherCode,
                    discountType = discountType,
                    discountValue = reward.voucherValue,
                    minOrderValue = if (reward.voucherValue > 0) reward.voucherValue * 2 else 0, // Example rule: Min order = 2x value
                    maxDiscountValue = reward.voucherValue, // Fixed value voucher usually has max = value
                    startDate = now,
                    endDate = expiryDate,
                    isActive = true,
                    applyFor = applyFor,
                    
                    // Private Voucher Settings
                    assignedUserIds = listOf(userId),
                    totalQuantity = 1,
                    userQuantities = mapOf(userId to 1),
                    usedByUserIds = emptyList() // Not used yet
                )
                
                transaction.set(newPromotionRef, newPromotion)
                
            }.await()
            
            Result.success(true)
        } catch (e: Exception) {
             Result.failure(e)
        }
    }
}
