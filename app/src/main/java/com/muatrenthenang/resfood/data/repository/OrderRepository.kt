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
}
