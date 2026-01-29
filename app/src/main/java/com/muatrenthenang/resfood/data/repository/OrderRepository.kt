package com.muatrenthenang.resfood.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.muatrenthenang.resfood.data.model.Order
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class OrderRepository {
    private val db = FirebaseFirestore.getInstance()
    private val ordersRef = db.collection("orders")

    suspend fun createOrder(order: Order): Result<String> {
        return try {
            val docRef = ordersRef.document()
            val finalOrder = order.copy(id = docRef.id)
            docRef.set(finalOrder).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllOrdersFlow(): Flow<List<Order>> = callbackFlow {
        val listener = ordersRef.orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Order::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                } ?: emptyList()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Boolean> {
        return try {
            ordersRef.document(orderId).update("status", newStatus).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectOrderWithReason(orderId: String, reason: String): Result<Boolean> {
        return try {
            ordersRef.document(orderId).update(
                mapOf(
                    "status" to "REJECTED",
                    "rejectionReason" to reason,
                    "rejectedAt" to com.google.firebase.Timestamp.now()
                )
            ).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    suspend fun getOrderById(orderId: String): Result<Order> {
         return try {
            val doc = ordersRef.document(orderId).get().await()
            val order = try {
                 doc.toObject(Order::class.java)
            } catch (e: Exception) {
                null
            }
            
            if (order != null) {
                Result.success(order)
            } else {
                Result.failure(Exception("Order not found or invalid format"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    fun getOrdersByUserId(userId: String): Flow<List<Order>> = callbackFlow {
        val listener = ordersRef.whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Order::class.java)
                    } catch (e: Exception) {
                        // Skip documents with incompatible format
                        e.printStackTrace()
                        null
                    }
                } ?: emptyList()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }
    
    /**
     * Recalculates total spending dynamically from COMPLETED orders.
     * This ensures data consistency.
     */
    suspend fun recalculateUserSpending(userId: String): Result<Double> {
        return try {
            val snapshot = ordersRef
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "COMPLETED")
                .get()
                .await()
                
            val total = snapshot.documents.sumOf { doc -> 
                doc.getLong("total") ?: 0L 
            }.toDouble()
            
            // Sync with User Profile
            val userRef = db.collection("users").document(userId)
            
            // Calculate Rank based on new total
            val newRank = com.muatrenthenang.resfood.data.model.Rank.getRankFromSpending(total)
            
            // Update User
            userRef.update(mapOf(
                "totalSpending" to total,
                "rank" to newRank.displayName
            )).await()
            
            Result.success(total)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun deleteOrder(orderId: String): Result<Boolean> {
        return try {
            ordersRef.document(orderId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Listen to a specific order updates in real-time
     */
    fun getOrderByIdFlow(orderId: String): Flow<Order?> = callbackFlow {
        val listener = ordersRef.document(orderId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                try {
                    val order = snapshot.toObject(Order::class.java)
                    trySend(order)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    /**
     * Mark order as reviewed
     */
    suspend fun markOrderAsReviewed(orderId: String): Result<Boolean> {
        return try {
            ordersRef.document(orderId).update("isReviewed", true).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
