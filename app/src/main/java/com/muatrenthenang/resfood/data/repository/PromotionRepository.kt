package com.muatrenthenang.resfood.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.muatrenthenang.resfood.data.model.Promotion
import kotlinx.coroutines.tasks.await

class PromotionRepository {
    private val db = FirebaseFirestore.getInstance()
    private val promosRef = db.collection("promotions")

    suspend fun createPromotion(promotion: Promotion): Result<Boolean> {
        return try {
            val docRef = promosRef.document()
            val finalPromo = promotion.copy(id = docRef.id)
            docRef.set(finalPromo).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllPromotions(): Result<List<Promotion>> {
        return try {
            val snapshot = promosRef.get().await()
            val promos = snapshot.toObjects(Promotion::class.java)
            Result.success(promos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Lấy danh sách promotions mà user có thể sử dụng:
     * - Promotions public (assignedUserIds rỗng) và user chưa dùng
     * - Promotions được gán cho user này và còn quota
     * - Chỉ lấy promotions còn hiệu lực (isActive = true, endDate > now)
     */
    suspend fun getPromotionsForUser(userId: String): Result<List<Promotion>> {
        return try {
            // Không filter isActive ở đây vì Firestore lưu field "active" (do serialization issue)
            // Sẽ filter bằng code sau khi deserialize
            val snapshot = promosRef.get().await()
            
            val allPromos = snapshot.toObjects(Promotion::class.java)
            
            // DEBUG: Log để xem dữ liệu
            android.util.Log.d("PromotionRepo", "=== DEBUG getPromotionsForUser ===")
            android.util.Log.d("PromotionRepo", "Current userId: $userId")
            android.util.Log.d("PromotionRepo", "Total promotions from Firestore: ${allPromos.size}")
            
            allPromos.forEach { promo ->
                android.util.Log.d("PromotionRepo", "---")
                android.util.Log.d("PromotionRepo", "Promo: ${promo.code}")
                android.util.Log.d("PromotionRepo", "  isPublic: ${promo.isPublic()}")
                android.util.Log.d("PromotionRepo", "  assignedUserIds: ${promo.assignedUserIds}")
                android.util.Log.d("PromotionRepo", "  userQuantities: ${promo.userQuantities}")
                android.util.Log.d("PromotionRepo", "  usedByUserIds size: ${promo.usedByUserIds.size}")
                android.util.Log.d("PromotionRepo", "  totalQuantity: ${promo.totalQuantity}")
                android.util.Log.d("PromotionRepo", "  endDate: ${promo.endDate.toDate()}")
                android.util.Log.d("PromotionRepo", "  canBeUsedBy($userId): ${promo.canBeUsedBy(userId)}")
            }
            
            // Lọc: user có thể dùng được (canBeUsedBy đã check hết hạn, quota, đã dùng...)
            val validPromos = allPromos.filter { promo ->
                promo.canBeUsedBy(userId)
            }
            
            android.util.Log.d("PromotionRepo", "Valid promotions: ${validPromos.size}")
            
            Result.success(validPromos)
        } catch (e: Exception) {
            android.util.Log.e("PromotionRepo", "Error: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Lấy promotion theo mã code (để validate khi áp dụng)
     */
    suspend fun getPromotionByCode(code: String, userId: String): Result<Promotion?> {
        return try {
            val snapshot = promosRef
                .whereEqualTo("code", code)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            val promo = snapshot.toObjects(Promotion::class.java).firstOrNull()
            
            if (promo == null) {
                Result.success(null)
            } else if (!promo.canBeUsedBy(userId)) {
                // Chi tiết lỗi cụ thể hơn
                val errorMsg = when {
                    promo.endDate < Timestamp.now() -> "Mã giảm giá đã hết hạn"
                    promo.isPublic() && promo.usedByUserIds.contains(userId) -> "Bạn đã sử dụng mã này rồi"
                    promo.isPublic() && promo.totalQuantity > 0 && promo.usedByUserIds.size >= promo.totalQuantity -> "Mã giảm giá đã hết lượt sử dụng"
                    !promo.isPublic() && !promo.assignedUserIds.contains(userId) -> "Mã giảm giá này không áp dụng cho bạn"
                    !promo.isPublic() && (promo.userQuantities[userId] ?: 0) <= 0 -> "Bạn đã hết lượt sử dụng mã này"
                    else -> "Mã giảm giá không hợp lệ"
                }
                Result.failure(Exception(errorMsg))
            } else {
                Result.success(promo)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sử dụng voucher (gọi sau khi đặt hàng thành công)
     * - Public: thêm userId vào usedByUserIds
     * - Private: giảm userQuantities[userId] đi 1
     */
    suspend fun useVoucher(promotionId: String, userId: String): Result<Boolean> {
        return try {
            val docRef = promosRef.document(promotionId)
            
            // Lấy promotion hiện tại để check loại
            val promoDoc = docRef.get().await()
            val promo = promoDoc.toObject(Promotion::class.java)
                ?: return Result.failure(Exception("Không tìm thấy mã giảm giá"))
            
            // Kiểm tra lại quyền sử dụng
            if (!promo.canBeUsedBy(userId)) {
                return Result.failure(Exception("Mã giảm giá không thể sử dụng"))
            }
            
            if (promo.isPublic()) {
                // PUBLIC: Thêm userId vào danh sách đã dùng
                docRef.update("usedByUserIds", FieldValue.arrayUnion(userId)).await()
            } else {
                // PRIVATE: Giảm quota của user đi 1
                val currentQuota = promo.userQuantities[userId] ?: 0
                if (currentQuota <= 0) {
                    return Result.failure(Exception("Bạn đã hết lượt sử dụng mã này"))
                }
                
                val newQuantities = promo.userQuantities.toMutableMap()
                newQuantities[userId] = currentQuota - 1
                docRef.update("userQuantities", newQuantities).await()
            }
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    /**
     * Khôi phục voucher (dùng sau khi hủy đơn hàng)
     */
    suspend fun restoreVoucher(promotionId: String, userId: String): Result<Boolean> {
        return try {
            val docRef = promosRef.document(promotionId)
            
            val promoDoc = docRef.get().await()
            val promo = promoDoc.toObject(Promotion::class.java)
                ?: return Result.failure(Exception("Không tìm thấy mã giảm giá"))
            
            if (promo.isPublic()) {
                // PUBLIC: Xóa userId khỏi danh sách đã dùng
                docRef.update("usedByUserIds", FieldValue.arrayRemove(userId)).await()
            } else {
                // PRIVATE: Tăng quota của user lên 1
                val currentQuota = promo.userQuantities[userId] ?: 0
                val newQuantities = promo.userQuantities.toMutableMap()
                newQuantities[userId] = currentQuota + 1
                docRef.update("userQuantities", newQuantities).await()
            }
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePromotion(promotion: Promotion): Result<Boolean> {
        return try {
            promosRef.document(promotion.id).set(promotion).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePromotion(promotionId: String): Result<Boolean> {
        return try {
            promosRef.document(promotionId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
