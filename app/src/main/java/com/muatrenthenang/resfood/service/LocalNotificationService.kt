package com.muatrenthenang.resfood.service

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.muatrenthenang.resfood.data.model.Order
import com.muatrenthenang.resfood.data.repository.AuthRepository
import com.muatrenthenang.resfood.util.CurrencyHelper
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
    private var adminReservationListener: ListenerRegistration? = null
    private var customerReservationListener: ListenerRegistration? = null
    
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
    private val settingsRepository = com.muatrenthenang.resfood.data.repository.SettingsRepository(context)

    // ... (rest of simple properties)

    // ... (init block)

    private fun startAdminListeners() {
        // 1. Listen for NEW orders
        adminOrderListener = db.collection("orders")
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener

                for (dc in snapshots!!.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        try {
                            val order = dc.document.toObject(Order::class.java)
                            
                            if (settingsRepository.isNotificationsEnabled()) {
                                NotificationHelper.showNewOrderNotification(context, order.id, order.total)
                            }
                            
                            val notif = com.muatrenthenang.resfood.data.model.Notification(
                                userId = auth.currentUser?.uid ?: "",
                                title = "Đơn hàng mới!",
                                body = "Đơn #${order.id.takeLast(6).uppercase()} - Total: ${CurrencyHelper.format(order.total)}",
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
            
        // 2. Listen for NEW Reservations
        adminReservationListener = db.collection("reservations")
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                
                for (dc in snapshots!!.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        try {
                            // Valid reservation found
                            val reservation = dc.document.toObject(com.muatrenthenang.resfood.data.model.TableReservation::class.java)
                            reservation.id = dc.document.id
                            
                            val timeStr = java.text.SimpleDateFormat("HH:mm dd/MM").format(reservation.timeSlot.toDate())
                            
                            // Fetch customer name
                            db.collection("users").document(reservation.userId).get()
                                .addOnSuccessListener { userDoc ->
                                    val customerName = userDoc.getString("fullName") ?: "Khách hàng"
                                    
                                    // Check settings before showing push notification
                                    if (settingsRepository.isNotificationsEnabled()) {
                                        NotificationHelper.showNewReservationNotification(
                                            context, reservation.id, customerName, timeStr
                                        )
                                    }
                                    
                                    val notif = com.muatrenthenang.resfood.data.model.Notification(
                                        userId = auth.currentUser?.uid ?: "",
                                        title = "Đặt bàn mới!",
                                        body = "$customerName đặt bàn lúc $timeStr",
                                        type = "new_reservation",
                                        referenceId = reservation.id,
                                        isRead = false,
                                        createdAt = com.google.firebase.Timestamp.now()
                                    )
                                    notificationRepository.createNotification(notif)
                                }
                                .addOnFailureListener {
                                    // Fallback if user fetch fails
                                    if (settingsRepository.isNotificationsEnabled()) {
                                        NotificationHelper.showNewReservationNotification(
                                            context, reservation.id, "Khách hàng", timeStr
                                        )
                                    }
                                }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
    }

    private fun startCustomerListeners(userId: String) {
        // 1. Listen for Order Updates
        customerOrderListener = db.collection("orders")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                
                for (dc in snapshots!!.documentChanges) {
                    if (dc.type == DocumentChange.Type.MODIFIED) {
                        val order = dc.document.toObject(Order::class.java)
                        
                        if (settingsRepository.isNotificationsEnabled()) {
                            NotificationHelper.showOrderStatusNotification(context, order.id, order.status)
                        }
                        
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
                            isRead = false,
                            createdAt = com.google.firebase.Timestamp.now()
                        )
                        notificationRepository.createNotification(notif)
                    }
                }
            }
            
        // 2. Listen for Reservation Updates
        customerReservationListener = db.collection("reservations")
            .whereEqualTo("user_id", userId) // Use correct field name "user_id" from TableReservation
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                
                for (dc in snapshots!!.documentChanges) {
                    if (dc.type == DocumentChange.Type.MODIFIED) {
                        val reservation = dc.document.toObject(com.muatrenthenang.resfood.data.model.TableReservation::class.java)
                        reservation.id = dc.document.id
                        
                        if (settingsRepository.isNotificationsEnabled()) {
                            NotificationHelper.showReservationStatusNotification(context, reservation.id, reservation.status)
                        }
                        
                        val statusText = when(reservation.status) {
                            "CONFIRMED" -> "được xác nhận"
                            "CANCELLED" -> "đã hủy"
                            "COMPLETED" -> "hoàn thành"
                            "REJECTED" -> "bị từ chối"
                            else -> reservation.status
                        }
                        
                        val notif = com.muatrenthenang.resfood.data.model.Notification(
                            userId = userId,
                            title = "Cập nhật đặt bàn",
                            body = "Lịch đặt bàn của bạn đã $statusText",
                            type = "reservation_update",
                            referenceId = reservation.id,
                            isRead = false,
                            createdAt = com.google.firebase.Timestamp.now()
                        )
                        notificationRepository.createNotification(notif)
                    }
                }
            }
    }

    fun stopListening() {
        adminOrderListener?.remove()
        customerOrderListener?.remove()
        adminReservationListener?.remove()
        customerReservationListener?.remove()
        
        adminOrderListener = null
        customerOrderListener = null
        adminReservationListener = null
        customerReservationListener = null
    }
}
