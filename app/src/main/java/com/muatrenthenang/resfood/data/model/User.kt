package com.muatrenthenang.resfood.data.model

import com.google.firebase.Timestamp
import java.security.MessageDigest
import java.util.Date

data class User(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val role: String = "customer", // "admin" hoặc "customer"
    val avatarUrl: String? = null,
    val phone: String? = null,
    val addresses: List<Address> = emptyList(), // Danh sách địa chỉ của user
    val rank: String = "Bạc", // Mặc định
    val points: Int = 0,
    val createdAt: Long = System.currentTimeMillis(), // Giữ nguyên Long để tương thích dữ liệu cũ
    
    // === Referral fields ===
    val referralCode: String = "",              // Mã giới thiệu của user (6 ký tự)
    val referredBy: String? = null,             // userId của người đã giới thiệu
    val referredUsers: List<String> = emptyList(), // Danh sách userId đã mời thành công
    val referralUsedAt: Long? = null            // Thời gian nhập mã (null = chưa nhập)
) {
    /**
     * Lấy địa chỉ mặc định của user
     */
    fun getDefaultAddress(): Address? {
        return addresses.find { it.isDefault } ?: addresses.firstOrNull()
    }
    
    /**
     * Chuyển createdAt sang Timestamp
     */
    fun getCreatedAtTimestamp(): Timestamp {
        return Timestamp(Date(createdAt))
    }
    
    /**
     * Kiểm tra user có thể nhập mã giới thiệu không (trong 24h đầu)
     */
    fun canUseReferralCode(): Boolean {
        // Đã nhập rồi thì không được nhập nữa
        if (referralUsedAt != null || referredBy != null) return false
        
        // Kiểm tra còn trong 24h không
        val diffMs = System.currentTimeMillis() - createdAt
        val diffHours = diffMs / (1000 * 60 * 60)
        return diffHours < 24
    }
    
    /**
     * Lấy số giờ còn lại để nhập mã (0 nếu hết thời gian)
     */
    fun getRemainingHoursForReferral(): Int {
        if (referralUsedAt != null || referredBy != null) return 0
        
        val diffMs = System.currentTimeMillis() - createdAt
        val diffHours = (diffMs / (1000 * 60 * 60)).toInt()
        return maxOf(0, 24 - diffHours)
    }
    
    companion object {
        /**
         * Tạo mã giới thiệu từ userId (10 ký tự uppercase)
         */
        fun generateReferralCode(userId: String): String {
            val hash = MessageDigest.getInstance("MD5")
                .digest(userId.toByteArray())
                .joinToString("") { "%02x".format(it) }
            return hash.take(10).uppercase()
        }
    }
}