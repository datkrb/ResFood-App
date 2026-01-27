package com.muatrenthenang.resfood.data.model

import java.util.UUID

/**
 * Model đại diện cho một địa chỉ giao hàng của người dùng
 */
data class Address(
    val id: String = UUID.randomUUID().toString(),
    val label: String = "Nhà riêng", // Nhà riêng, Công ty, Khác
    val addressLine: String = "",
    val ward: String = "", // Phường/Xã
    val district: String = "", // Quận/Huyện
    val city: String = "", // Thành phố
    val contactName: String = "",
    val phone: String = "",
    val isDefault: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Trả về địa chỉ đầy đủ dạng chuỗi
     */
    fun getFullAddress(): String {
        val parts = listOfNotNull(
            addressLine.ifBlank { null },
            ward.ifBlank { null },
            district.ifBlank { null },
            city.ifBlank { null }
        )
        return parts.joinToString(", ")
    }

    /**
     * Kiểm tra địa chỉ có hợp lệ không
     */
    fun isValid(): Boolean {
        return addressLine.isNotBlank() &&
                contactName.isNotBlank() &&
                phone.isNotBlank() &&
                city.isNotBlank()
    }
}

/**
 * Các loại nhãn địa chỉ có sẵn
 */
object AddressLabels {
    val labels = listOf("Nhà riêng", "Công ty", "Khác")
}
