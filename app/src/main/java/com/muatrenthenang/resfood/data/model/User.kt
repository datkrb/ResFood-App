package com.muatrenthenang.resfood.data.model

data class User(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val role: String = "customer", // "admin" hoặc "customer"
    val avatarUrl: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)