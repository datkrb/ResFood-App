package com.muatrenthenang.resfood.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.muatrenthenang.resfood.data.model.Promotion
import com.muatrenthenang.resfood.data.model.User
import kotlinx.coroutines.tasks.await

/**
 * Repository xử lý logic mời bạn bè
 */
class ReferralRepository {
    
    private val db = FirebaseFirestore.getInstance()
    private val usersRef = db.collection("users")
    private val promosRef = db.collection("promotions")
    
    companion object {
        // ID của promotion dành cho referral (tạo 1 lần trong Firestore)
        const val REFERRAL_PROMOTION_ID = "referral_voucher"
        const val REFERRAL_VOUCHER_NAME = "Voucher mời bạn bè"
        const val REFERRAL_VOUCHER_VALUE = 50000
        const val REFERRAL_VOUCHER_MIN_ORDER = 100000
    }
    
    /**
     * Tìm user theo mã giới thiệu
     */
    suspend fun findUserByReferralCode(code: String): Result<User?> {
        return try {
            val snapshot = usersRef
                .whereEqualTo("referralCode", code.uppercase())
                .limit(1)
                .get()
                .await()
            
            val user = snapshot.toObjects(User::class.java).firstOrNull()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Áp dụng mã giới thiệu
     * @param referrerUserId ID người giới thiệu (người có mã)
     * @param newUserId ID người mới (người nhập mã)
     */
    suspend fun applyReferralCode(referrerUserId: String, newUserId: String): Result<Boolean> {
        return try {
            // 1. Cập nhật người mới: đánh dấu đã dùng mã
            usersRef.document(newUserId).update(
                mapOf(
                    "referredBy" to referrerUserId,
                    "referralUsedAt" to System.currentTimeMillis()
                )
            ).await()
            
            // 2. Cập nhật người giới thiệu: thêm vào danh sách đã mời
            usersRef.document(referrerUserId).update(
                "referredUsers", FieldValue.arrayUnion(newUserId)
            ).await()
            
            // 3. Tặng voucher cho cả 2
            grantReferralVoucher(referrerUserId).getOrThrow()
            grantReferralVoucher(newUserId).getOrThrow()
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Tặng voucher referral cho user
     * Nếu đã có thì tăng số lượng, chưa có thì thêm mới
     */
    suspend fun grantReferralVoucher(userId: String): Result<Boolean> {
        return try {
            val promoDoc = promosRef.document(REFERRAL_PROMOTION_ID).get().await()
            
            if (!promoDoc.exists()) {
                // Tạo promotion mới nếu chưa có
                createReferralPromotion()
            }
            
            // Lấy promotion hiện tại
            val promo = promosRef.document(REFERRAL_PROMOTION_ID).get().await()
                .toObject(Promotion::class.java)
                ?: return Result.failure(Exception("Không tìm thấy promotion"))
            
            // Cập nhật userQuantities
            val currentQty = promo.userQuantities[userId] ?: 0
            val newQuantities = promo.userQuantities.toMutableMap()
            newQuantities[userId] = currentQty + 1
            
            // Thêm userId vào assignedUserIds nếu chưa có
            val updatedAssignedUsers = if (promo.assignedUserIds.contains(userId)) {
                promo.assignedUserIds
            } else {
                promo.assignedUserIds + userId
            }
            
            promosRef.document(REFERRAL_PROMOTION_ID).update(
                mapOf(
                    "userQuantities" to newQuantities,
                    "assignedUserIds" to updatedAssignedUsers
                )
            ).await()
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Tạo promotion cho referral (gọi 1 lần)
     */
    private suspend fun createReferralPromotion() {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.YEAR, 10) // Hạn 10 năm
        val endDate = Timestamp(calendar.time)
        
        val promo = Promotion(
            id = REFERRAL_PROMOTION_ID,
            name = REFERRAL_VOUCHER_NAME,
            code = "REFERRAL",
            discountType = 1, // Amount
            discountValue = REFERRAL_VOUCHER_VALUE,
            minOrderValue = REFERRAL_VOUCHER_MIN_ORDER,
            maxDiscountValue = REFERRAL_VOUCHER_VALUE,
            startDate = Timestamp.now(),
            endDate = endDate,
            isActive = true,
            applyFor = "ALL",
            assignedUserIds = emptyList(), // Sẽ thêm userId khi tặng
            userQuantities = emptyMap()
        )
        
        promosRef.document(REFERRAL_PROMOTION_ID).set(promo).await()
    }
    
    /**
     * Lấy danh sách user đã mời thành công
     */
    suspend fun getReferredUsers(userId: String): Result<List<User>> {
        return try {
            val userDoc = usersRef.document(userId).get().await()
            val user = userDoc.toObject(User::class.java)
                ?: return Result.success(emptyList())
            
            if (user.referredUsers.isEmpty()) {
                return Result.success(emptyList())
            }
            
            // Lấy thông tin các user đã mời
            val referredUsers = mutableListOf<User>()
            for (referredId in user.referredUsers) {
                val referredDoc = usersRef.document(referredId).get().await()
                referredDoc.toObject(User::class.java)?.let { referredUsers.add(it) }
            }
            
            Result.success(referredUsers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cập nhật referralCode cho user (gọi khi đăng ký)
     */
    suspend fun initReferralCode(userId: String): Result<String> {
        return try {
            val code = User.generateReferralCode(userId)
            usersRef.document(userId).update("referralCode", code).await()
            Result.success(code)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
