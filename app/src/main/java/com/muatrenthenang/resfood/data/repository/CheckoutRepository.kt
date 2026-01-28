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
                val toppingsList = doc.get("toppings") as? List<HashMap<String, Any>> ?: emptyList()
                val toppings = toppingsList.mapNotNull {
                    try {
                        com.muatrenthenang.resfood.data.model.Topping(
                            id = it["id"] as? String ?: "",
                            name = it["name"] as? String ?: "",
                            price = (it["price"] as? Long)?.toInt() ?: 0,
                            imageUrl = it["imageUrl"] as? String ?: ""
                        )
                    } catch (e: Exception) { null }
                }

                if (food != null) {
                    items.add(CartItem(
                        id = doc.id,
                        food = food,
                        quantity = quantity,
                        isSelected = isSelected,
                        note = note,
                        toppings = toppings
                    ))
                }
            }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Xóa các item trong cart mà isSelected = true
    suspend fun removeSelectedItems(): Result<Boolean> {
        val userId = auth.currentUser?.uid
            ?: return Result.failure(Exception("User chưa đăng nhập"))
        return try {
            val cartRef = db.collection("carts").document(userId).collection("cartItems")
            val snapshot = cartRef.whereEqualTo("isSelected", true).get().await()
            if (snapshot.isEmpty) return Result.success(true)
            val batch = db.batch()
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
