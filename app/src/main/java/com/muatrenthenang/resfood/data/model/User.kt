package com.muatrenthenang.resfood.data.model

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
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Lấy địa chỉ mặc định của user
     */
    fun getDefaultAddress(): Address? {
        return addresses.find { it.isDefault } ?: addresses.firstOrNull()
    }
}