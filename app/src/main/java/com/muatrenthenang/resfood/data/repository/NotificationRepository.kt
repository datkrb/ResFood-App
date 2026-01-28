package com.muatrenthenang.resfood.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.muatrenthenang.resfood.data.model.Notification
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val notificationsCollection = db.collection("notifications")

    // Listen to all notifications for the current user
    fun getUserNotifications(): Flow<List<Notification>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            close()
            return@callbackFlow
        }

        val listener = notificationsCollection
            .whereEqualTo("userId", userId)
            // .orderBy("createdAt", Query.Direction.DESCENDING) // Removed to avoid missing index crash
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Log error but don't crash flow immediately if possible, or handle in VM
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    var notifications = snapshot.toObjects(Notification::class.java)
                    // Sort in memory
                    notifications = notifications.sortedByDescending { it.createdAt }
                    trySend(notifications)
                }
            }

        awaitClose { listener.remove() }
    }
    
    // Listen to unread count only (for the badge)
    fun getUnreadCount(): Flow<Int> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
             trySend(0)
             close()
             return@callbackFlow
        }

        val listener = notificationsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.size())
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun markAsRead(notificationId: String) {
        try {
            notificationsCollection.document(notificationId)
                .update("isRead", true)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun markAllAsRead() {
        val userId = auth.currentUser?.uid ?: return
        try {
            // Batch update for better performance
            val snapshot = notificationsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get().await()

            val batch = db.batch()
            for (doc in snapshot.documents) {
               batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Helper to create a notification (used by LocalService)
    fun createNotification(notification: Notification) {
        val docRef = notificationsCollection.document()
        val newNotif = notification.copy(id = docRef.id)
        docRef.set(newNotif)
    }
}
