package com.muatrenthenang.resfood.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.muatrenthenang.resfood.MainActivity
import com.muatrenthenang.resfood.R
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object NotificationHelper {

    const val ADMIN_CHANNEL_ID = "admin_channel_v2"
    const val CUSTOMER_CHANNEL_ID = "customer_channel_v2"
    
    // Notification IDs
    private const val ORDER_NOTIFICATION_ID = 1001
    private const val RESERVATION_NOTIFICATION_ID = 1002

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val adminChannel = NotificationChannel(
                ADMIN_CHANNEL_ID,
                "Admin Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new orders and reservations"
                enableVibration(true)
            }

            val customerChannel = NotificationChannel(
                CUSTOMER_CHANNEL_ID,
                "Order Updates",
                NotificationManager.IMPORTANCE_HIGH // Upgraded to HIGH for popup
            ).apply {
                description = "Notifications for order status updates"
                enableVibration(true)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(adminChannel)
            manager.createNotificationChannel(customerChannel)
        }
    }

    fun showNewOrderNotification(context: Context, orderId: String, total: Int) {
        if (!hasPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Add extra to navigate to OrderDetail if possible, or just open app
            putExtra("navigate_to", "order_detail")
            putExtra("order_id", orderId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, ADMIN_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with app icon
            .setContentTitle("New Order Received!")
            .setContentText("Order #$orderId - Total: ${total}đ")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Sound + Vibrate
            .setVibrate(longArrayOf(0, 500)) // Explicit vibration
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(ORDER_NOTIFICATION_ID + orderId.hashCode(), builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun showOrderStatusNotification(context: Context, orderId: String, status: String) {
        if (!hasPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "order_detail")
            putExtra("order_id", orderId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CUSTOMER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with app icon
            .setContentTitle("Order Update")
            .setContentText("Your order #$orderId is now $status")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Upgraded to HIGH
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Sound + Vibrate
            .setVibrate(longArrayOf(0, 500)) // Explicit vibration
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(ORDER_NOTIFICATION_ID + orderId.hashCode(), builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
    
    private fun hasPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
             if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                 return false
             }
        }
        return true
    }
}
