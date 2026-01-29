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
                // CartItem Document ID
                val docId = doc.id
                
                val foodId = doc.getString("foodId") ?: ""
                val quantity = doc.getLong("quantity")?.toInt() ?: 1
                val isSelected = doc.getBoolean("isSelected") ?: true
                val note = doc.getString("note")
                
                // Parse Toppings
                val toppingsList = doc.get("toppings") as? List<Map<String, Any>> ?: emptyList()
                val toppings = toppingsList.map { map ->
                    com.muatrenthenang.resfood.data.model.Topping(
                        name = map["name"] as? String ?: "",
                        price = (map["price"] as? Long)?.toInt() ?: 0,
                        imageUrl = map["imageUrl"] as? String
                    )
                }

                if (foodId.isNotEmpty()) {
                    val foodDoc = db.collection("foods").document(foodId).get().await()
                    val food = foodDoc.toObject(Food::class.java)?.copy(id = foodId)
                    if (food != null) {
                        items.add(
                            CartItem(
                                id = docId, // Important: Use Document ID
                                food = food,
                                quantity = quantity,
                                isSelected = isSelected,
                                note = note,
                                toppings = toppings
                            )
                        )
                    }
                }
            }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Thêm hoặc cập nhật 1 item vào cart
    // Logic mới: Một món + toppings khác nhau = Item khác nhau
    // Cần check xem đã có item nào trùng foodId AND trùng list toppings chưa
    suspend fun addOrUpdateCartItem(
        foodId: String, 
        quantity: Int, 
        note: String? = null, 
        isSelected: Boolean = true,
        toppings: List<com.muatrenthenang.resfood.data.model.Topping> = emptyList(),
        isAccumulate: Boolean = false // New param: true = add to existing, false = set quantity
    ): Result<Boolean> {
        val userId = auth.currentUser?.uid
            ?: return Result.failure(Exception("User chưa đăng nhập"))
            
        return try {
            val cartRef = db.collection("carts").document(userId).collection("cartItems")
            
            // 1. Get all items to find match
            val snapshot = cartRef.get().await()
            
            var existingDocId: String? = null
            var currentQuantity = 0
            
            for (doc in snapshot) {
                val fId = doc.getString("foodId")
                if (fId == foodId) {
                    val dbToppingsList = doc.get("toppings") as? List<Map<String, Any>> ?: emptyList()
                    val dbToppings = dbToppingsList.map { map ->
                        com.muatrenthenang.resfood.data.model.Topping(
                            name = map["name"] as? String ?: "",
                            price = (map["price"] as? Long)?.toInt() ?: 0,
                            imageUrl = map["imageUrl"] as? String
                        )
                    }
                    
                    if (dbToppings.size == toppings.size && dbToppings.containsAll(toppings) && toppings.containsAll(dbToppings)) {
                        existingDocId = doc.id
                        currentQuantity = doc.getLong("quantity")?.toInt() ?: 0
                        break
                    }
                }
            }

            if (existingDocId != null) {
                val finalQuantity = if (isAccumulate) currentQuantity + quantity else quantity
                
                cartRef.document(existingDocId).update(mapOf(
                    "quantity" to finalQuantity,
                    "note" to note,
                    "isSelected" to isSelected,
                    "toppings" to toppings
                )).await()
            } else {
                val newItem = mapOf(
                    "foodId" to foodId,
                    "quantity" to quantity,
                    "note" to note,
                    "isSelected" to isSelected,
                    "toppings" to toppings
                )
                cartRef.add(newItem).await()
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Xóa 1 item khỏi cart (by Doc ID)
    suspend fun removeCartItem(cartItemId: String): Result<Boolean> {
        val userId = auth.currentUser?.uid
            ?: return Result.failure(Exception("User chưa đăng nhập"))
        return try {
            db.collection("carts")
                .document(userId)
                .collection("cartItems")
                .document(cartItemId)
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
