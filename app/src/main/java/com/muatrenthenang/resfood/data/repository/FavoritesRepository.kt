package com.muatrenthenang.resfood.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.muatrenthenang.resfood.data.model.FavoriteItem
import com.muatrenthenang.resfood.data.model.Food
import kotlinx.coroutines.tasks.await

class FavoritesRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val TAG = "FavoritesRepository"

    // Lấy danh sách yêu thích của user hiện tại
    suspend fun getFavorites(): Result<List<FavoriteItem>> {
        val userId = auth.currentUser?.uid
            ?: return Result.failure(Exception("User chưa đăng nhập"))
        return try {
            Log.d(TAG, "Fetching favorites for user: $userId")
            val favSnapshot = db.collection("favorites")
                .document(userId)
                .collection("items")
                .get()
                .await()
            val favorites = mutableListOf<FavoriteItem>()
            for (doc in favSnapshot) {
                val foodId = doc.id
                val addedAt = doc.getLong("addedAt") ?: System.currentTimeMillis()
                val foodDoc = db.collection("foods").document(foodId).get().await()
                val food = foodDoc.toObject(Food::class.java)?.copy(id = foodId)
                if (food != null) {
                    favorites.add(FavoriteItem(food, addedAt))
                }
            }
            Result.success(favorites)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Thêm một món vào danh sách yêu thích
    suspend fun addFavorite(foodId: String): Result<Boolean> {
        val userId = auth.currentUser?.uid
            ?: return Result.failure(Exception("User chưa đăng nhập"))
        return try {
            db.collection("favorites")
                .document(userId)
                .collection("items")
                .document(foodId)
                .set(mapOf("addedAt" to System.currentTimeMillis()))
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Xóa một món khỏi danh sách yêu thích
    suspend fun removeFavorite(foodId: String): Result<Boolean> {
        val userId = auth.currentUser?.uid
            ?: return Result.failure(Exception("User chưa đăng nhập"))
        return try {
            db.collection("favorites")
                .document(userId)
                .collection("items")
                .document(foodId)
                .delete()
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
