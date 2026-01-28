package com.muatrenthenang.resfood.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

/**
 * Mã giảm giá / Khuyến mãi
 * 
 * Có 2 loại:
 * 1. PUBLIC (assignedUserIds rỗng): 
 *    - totalQuantity: tổng số lượng voucher
 *    - usedByUserIds: danh sách userId đã dùng (mỗi user chỉ dùng 1 lần)
 * 
 * 2. PRIVATE (assignedUserIds có giá trị):
 *    - userQuantities: Map<userId, số lượng còn lại> - mỗi user có quota riêng
 */
data class Promotion(
    val id: String = "",
    val name: String = "",
    val code: String = "",
    val discountType: Int = 0, // 0: %, 1: Amount
    val discountValue: Int = 0,
    val minOrderValue: Int = 0,
    val maxDiscountValue: Int = 0,
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now(),
    
    @get:PropertyName("isActive") @set:PropertyName("isActive")
    var isActive: Boolean = true,
    
    val applyFor: String = "ALL", // ALL, SHIP
    
    // === Cho voucher PUBLIC (tất cả mọi người) ===
    val assignedUserIds: List<String> = emptyList(), // Rỗng = public
    val totalQuantity: Int = 0, // Tổng số lượng voucher (0 = không giới hạn)
    val usedByUserIds: List<String> = emptyList(), // Danh sách userId đã dùng
    
    // === Cho voucher PRIVATE (riêng một số user) ===
    val userQuantities: Map<String, Int> = emptyMap() // userId -> số lượng còn lại
) {
    /**
     * Kiểm tra voucher có phải public (cho tất cả) không
     */
    fun isPublic(): Boolean = assignedUserIds.isEmpty()
    
    /**
     * Kiểm tra user có quyền dùng voucher này không
     */
    fun canBeUsedBy(userId: String): Boolean {
        // Kiểm tra hết hạn
        if (endDate < Timestamp.now()) return false
        if (!isActive) return false
        
        return if (isPublic()) {
            // PUBLIC: Kiểm tra còn số lượng và user chưa dùng
            val hasQuantity = totalQuantity == 0 || usedByUserIds.size < totalQuantity
            val notUsedYet = !usedByUserIds.contains(userId)
            hasQuantity && notUsedYet
        } else {
            // PRIVATE: Kiểm tra user được assign và còn quota
            val userQuota = userQuantities[userId] ?: 0
            assignedUserIds.contains(userId) && userQuota > 0
        }
    }
    
    /**
     * Lấy số lượng voucher còn lại cho user
     */
    fun getRemainingQuantity(userId: String): Int {
        return if (isPublic()) {
            if (usedByUserIds.contains(userId)) 0
            else if (totalQuantity == 0) 1 // Không giới hạn, user dùng được 1 lần
            else maxOf(0, totalQuantity - usedByUserIds.size)
        } else {
            userQuantities[userId] ?: 0
        }
    }
}
