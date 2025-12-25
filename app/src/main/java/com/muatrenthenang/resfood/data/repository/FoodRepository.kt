package com.muatrenthenang.resfood.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.muatrenthenang.resfood.data.model.Food
import com.muatrenthenang.resfood.data.model.Review
import kotlinx.coroutines.tasks.await

class FoodRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private suspend fun checkAdmin(): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("User chưa đăng nhập"))
        val userDoc = db.collection("users").document(user.uid).get().await()
        val role = userDoc.getString("role")
        return if (role == "admin") Result.success(Unit)
        else Result.failure(Exception("Bạn không có quyền admin"))
    }

    // Lấy danh sách tất cả món ăn
    suspend fun getFoods(): Result<List<Food>> {
        return try {
            val snapshot = db.collection("foods").get().await()
            val items = snapshot.mapNotNull { it.toObject(Food::class.java)?.copy(id = it.id) }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Lấy 1 món ăn theo id
    suspend fun getFood(foodId: String): Result<Food> {
        return try {
            val doc = db.collection("foods").document(foodId).get().await()
            val food = doc.toObject(Food::class.java)?.copy(id = doc.id)
            if (food != null) Result.success(food)
            else Result.failure(Exception("Không tìm thấy món ăn"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Thêm món ăn (chỉ admin), id sẽ được tạo tự động
    suspend fun addFood(food: Food): Result<Food> {
        checkAdmin().onFailure { return Result.failure(it) }
        return try {
            val docRef = db.collection("foods").add(food).await()
            val foodWithId = food.copy(id = docRef.id)
            db.collection("foods")
                .document(docRef.id)
                .set(foodWithId)
                .await()

            Result.success(foodWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cập nhật món ăn (chỉ admin)
    suspend fun updateFood(food: Food): Result<Boolean> {
        checkAdmin().onFailure { return Result.failure(it) }
        val foodId = food.id ?: return Result.failure(Exception("Thiếu id món ăn"))
        return try {
            db.collection("foods").document(foodId).set(food).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Xóa món ăn (chỉ admin)
    suspend fun deleteFood(foodId: String): Result<Boolean> {
        checkAdmin().onFailure { return Result.failure(it) }
        return try {
            db.collection("foods").document(foodId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Gửi đánh giá cho món ăn
    suspend fun submitReview(
        foodId: String,
        rating: Int,
        comment: String,
        userId: String,
        userName: String
    ): Result<Boolean> {
        return try {
            // Tạo review object
            val review = Review(
                star = rating,
                comment = comment,
                userId = userId,
                userName = userName,
                createdAt = System.currentTimeMillis()
            )

            // Lấy thông tin món ăn hiện tại
            val foodResult = getFood(foodId)
            val food = foodResult.getOrNull()
                ?: return Result.failure(Exception("Không tìm thấy món ăn"))

            // Thêm review vào danh sách reviews
            val updatedReviews = food.reviews.toMutableList()
            updatedReviews.add(review)

            // Tính lại rating trung bình
            val averageRating = updatedReviews.map { it.star }.average().toFloat()

            // Cập nhật món ăn với review mới và rating mới
            val updatedFood = food.copy(
                reviews = updatedReviews,
                rating = averageRating
            )

            // Lưu vào Firestore
            db.collection("foods").document(foodId).set(updatedFood).await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
