package com.muatrenthenang.resfood.data.model

data class Food(
    /** ID món ăn */
    val id: String = "",
    /** Tên món ăn */
    val name: String = "",
    /** Link ảnh món ăn */
    val imageUrl: String? = null,
    /** Giá món ăn (VNĐ) */
    val price: Int = 0,
    /** Tỉ lệ giảm giá (%) */
    val discountPercent: Int = 0,
    /** Chỉ số năng lượng (kcal) */
    val calories: Int = 0,
    /** Mô tả ngắn gọn món ăn */
    val description: String = "",
    /** Điểm đánh giá trung bình */
    val rating: Float = 0f,
    /** Danh sách đánh giá */
    val reviews: List<Review> = emptyList(),
    /** Còn bán hay không */
    val isAvailable: Boolean = true
)
