package com.muatrenthenang.resfood.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.muatrenthenang.resfood.data.model.CartItem
import com.muatrenthenang.resfood.data.model.Food
import kotlinx.coroutines.tasks.await

class CheckoutRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Lấy danh sách cart của user hiện tại, chỉ lấy các item isSelected = true
    suspend fun getSelectedCartItems(): Result<List<CartItem>> {
        val userId = auth.currentUser?.uid
            ?: return Result.failure(Exception("User chưa đăng nhập"))
        return try {
            val snapshot = db.collection("carts")
                .document(userId)
                .collection("cartItems")
                .whereEqualTo("isSelected", true)
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
}
