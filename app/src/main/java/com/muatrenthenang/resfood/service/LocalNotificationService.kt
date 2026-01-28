package com.muatrenthenang.resfood.service

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.muatrenthenang.resfood.data.model.Order
import com.muatrenthenang.resfood.data.repository.AuthRepository
import com.muatrenthenang.resfood.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Service to listen for Firestore updates and trigger local notifications.
 * This should be initialized when the app starts.
 */
class LocalNotificationService(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    private var adminOrderListener: ListenerRegistration? = null
    private var customerOrderListener: ListenerRegistration? = null
    
    init {
        // Observe auth state changes to switch listeners
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                checkRoleAndStartListening(user.uid)
            } else {
                stopListening()
            }
        }
    }

    private fun checkRoleAndStartListening(userId: String) {
        scope.launch {
            try {
                // Determine role
                val userDoc = db.collection("users").document(userId).get().await()
                val role = userDoc.getString("role") ?: "customer"
                
                stopListening() // Clear existing listeners
                
                if (role == "admin") {
                    startAdminListeners()
                } else {
                    startCustomerListeners(userId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val notificationRepository = com.muatrenthenang.resfood.data.repository.NotificationRepository()

    // ... (rest of simple properties)

    // ... (init block)

    private fun startAdminListeners() {
        // Listen for NEW orders (added)
        adminOrderListener = db.collection("orders")
            .whereEqualTo("status", "PENDING") // Or listen to all, but focus on PENDING for new alerts
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener

                for (dc in snapshots!!.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        try {
                            val order = dc.document.toObject(Order::class.java)
                            // Only notify if created reasonably recently (e.g. last 5 mins)
                            // For simplicity, we trigger
                            
                            // 1. Show System Notification
                            NotificationHelper.showNewOrderNotification(context, order.id, order.total)
                            
                            // 2. Persist to Firestore implementation
                            // Note: In a real app, backend cloud functions should create this doc.
                            // But for this client-side demo, we create it here.
                            val notif = com.muatrenthenang.resfood.data.model.Notification(
                                userId = auth.currentUser?.uid ?: "",
                                title = "Đơn hàng mới!",
                                body = "Đơn #${order.id.takeLast(6).uppercase()} - Total: ${order.total}đ",
                                type = "new_order",
                                referenceId = order.id,
                                isRead = false
                            )
                            notificationRepository.createNotification(notif)
                            
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
    }

    private fun startCustomerListeners(userId: String) {
        // Listen for updates to MY orders
        customerOrderListener = db.collection("orders")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                
                for (dc in snapshots!!.documentChanges) {
                    if (dc.type == DocumentChange.Type.MODIFIED) {
                        val order = dc.document.toObject(Order::class.java)
                        
                        // 1. Show System Notification
                        NotificationHelper.showOrderStatusNotification(context, order.id, order.status)
                        
                        // 2. Persist to Firestore
                        val statusText = when(order.status) {
                            "PENDING" -> "Đang chờ duyệt"
                            "PROCESSING" -> "Đang chuẩn bị"
                            "DELIVERING" -> "Đang giao hàng"
                            "COMPLETED" -> "Đã hoàn thành"
                            "CANCELLED" -> "Đã hủy"
                            "REJECTED" -> "Bị từ chối"
                            else -> order.status
                        }
                        
                        val notif = com.muatrenthenang.resfood.data.model.Notification(
                            userId = userId,
                            title = "Cập nhật đơn hàng",
                            body = "Đơn hàng #${order.id.takeLast(6).uppercase()} của bạn hiện $statusText",
                            type = "order_update",
                            referenceId = order.id,
                            isRead = false
                        )
                        notificationRepository.createNotification(notif)
                    }
                }
            }
    }

    fun stopListening() {
        adminOrderListener?.remove()
        customerOrderListener?.remove()
        adminOrderListener = null
        customerOrderListener = null
    }
}
