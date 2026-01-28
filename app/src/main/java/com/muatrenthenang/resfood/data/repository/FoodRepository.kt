package com.muatrenthenang.resfood.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.muatrenthenang.resfood.data.model.Food
import com.muatrenthenang.resfood.data.model.Review
import kotlinx.coroutines.tasks.await

class FoodRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getCurrentUserId(): String? = auth.currentUser?.uid

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
    // Thêm review (người dùng bình thường)
    suspend fun addReview(foodId: String, review: Review): Result<Boolean> {
        return try {
            val docRef = db.collection("foods").document(foodId)
            
            // Run transaction to ensure atomic update of reviews and rating
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val currentFood = snapshot.toObject(Food::class.java) 
                    ?: throw Exception("Food not found")
                
                val currentReviews = currentFood.reviews.toMutableList()
                currentReviews.add(0, review) // Add new review to top
                
                // Recalculate rating
                val newRating = if (currentReviews.isNotEmpty()) {
                    currentReviews.map { it.star }.average().toFloat()
                } else {
                    0f
                }
                
                transaction.update(docRef, "reviews", currentReviews)
                transaction.update(docRef, "rating", newRating)
            }.await()
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
