package com.muatrenthenang.resfood.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.muatrenthenang.resfood.data.model.CartItem
import com.muatrenthenang.resfood.data.model.Food
import kotlinx.coroutines.tasks.await

class CartRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Lấy danh sách cart của user hiện tại
    suspend fun getCartItems(): Result<List<CartItem>> {
        val userId = auth.currentUser?.uid
            ?: return Result.failure(Exception("User chưa đăng nhập"))
        return try {
            val snapshot = db.collection("carts")
                .document(userId)
                .collection("cartItems")
                .get()
                .await()
            val items = mutableListOf<CartItem>()
            for (doc in snapshot) {
                val foodId = doc.getString("foodId") ?: doc.id
                val quantity = doc.getLong("quantity")?.toInt() ?: 1
                val isSelected = doc.getBoolean("isSelected") ?: true
                val note = doc.getString("note")
                val foodDoc = db.collection("foods").document(foodId).get().await()
                val food = foodDoc.toObject(Food::class.java)?.copy(id = foodId)
                if (food != null) {
                    items.add(CartItem(food, quantity, isSelected, note))
                }
            }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Thêm hoặc cập nhật 1 item vào cart
    suspend fun addOrUpdateCartItem(foodId: String, quantity: Int, note: String? = null, isSelected: Boolean = true): Result<Boolean> {
        val userId = auth.currentUser?.uid
            ?: return Result.failure(Exception("User chưa đăng nhập"))
        return try {
            db.collection("carts")
                .document(userId)
                .collection("cartItems")
                .document(foodId)
                .set(mapOf(
                    "foodId" to foodId,
                    "quantity" to quantity,
                    "note" to note,
                    "isSelected" to isSelected
                ))
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Xóa 1 item khỏi cart
    suspend fun removeCartItem(foodId: String): Result<Boolean> {
        val userId = auth.currentUser?.uid
            ?: return Result.failure(Exception("User chưa đăng nhập"))
        return try {
            db.collection("carts")
                .document(userId)
                .collection("cartItems")
                .document(foodId)
                .delete()
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Xóa toàn bộ cart
    suspend fun clearCart(): Result<Boolean> {
        val userId = auth.currentUser?.uid
            ?: return Result.failure(Exception("User chưa đăng nhập"))
        return try {
            val batch = db.batch()
            val cartRef = db.collection("carts").document(userId).collection("cartItems")
            val snapshot = cartRef.get().await()
            for (doc in snapshot) {
                batch.delete(doc.reference)
            }
            batch.commit().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
