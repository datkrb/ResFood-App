package com.muatrenthenang.resfood.data.model

data class Review(
    /** Số sao đánh giá (1-5) */
    val star: Int = 5,
    /** Nội dung đánh giá */
    val comment: String = "",
    /** ID người đánh giá */
    val userId: String = "",
    /** Tên người đánh giá */
    val userName: String = "",
    /** Thời gian đánh giá (timestamp) */
    val createdAt: Long = System.currentTimeMillis()
)
