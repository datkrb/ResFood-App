package com.muatrenthenang.resfood.data.local

import com.muatrenthenang.resfood.data.model.Review

/**
 * Mock storage để lưu reviews tạm thời local (chưa push lên Firebase)
 * Dùng cho testing/mock purposes
 */
object MockReviewStorage {
    // Map: foodId -> List<Review>
    private val reviewsMap = mutableMapOf<String, MutableList<Review>>()

    /**
     * Thêm review vào storage local
     */
    fun addReview(foodId: String, review: Review) {
        val reviews = reviewsMap.getOrPut(foodId) { mutableListOf() }
        reviews.add(review)
    }

    /**
     * Lấy tất cả reviews của một món ăn
     */
    fun getReviews(foodId: String): List<Review> {
        return reviewsMap[foodId]?.toList() ?: emptyList()
    }

    /**
     * Tính rating trung bình của một món ăn
     */
    fun getAverageRating(foodId: String): Float {
        val reviews = reviewsMap[foodId] ?: return 0f
        if (reviews.isEmpty()) return 0f
        return reviews.map { it.star }.average().toFloat()
    }

    /**
     * Xóa tất cả reviews (dùng khi muốn reset)
     */
    fun clearAll() {
        reviewsMap.clear()
    }

    /**
     * Xóa reviews của một món ăn cụ thể
     */
    fun clearReviews(foodId: String) {
        reviewsMap.remove(foodId)
    }
}

