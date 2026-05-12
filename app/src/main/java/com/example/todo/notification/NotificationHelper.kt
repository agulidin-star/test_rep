package com.example.todo.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.todo.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID_DEADLINES = "deadlines_channel"
        const val CHANNEL_NAME = "Deadlines"
        const val CHANNEL_DESCRIPTION = "Notifications for task deadlines"
    }

    fun createNotificationChannel() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID_DEADLINES) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID_DEADLINES,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun notify(notificationId: Int, notification: NotificationCompat.Builder) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification.build())
    }

    companion object {
        fun createMarkDoneIntent(context: Context, taskId: Long): Intent {
            return Intent(context, NotificationActionReceiver::class.java)
                .setAction(com.example.todo.notification.NotificationActionReceiver.ACTION_MARK_DONE)
                .putExtra("taskId", taskId)
        }

        fun createPendingIntent(context: Context, intent: Intent, requestCode: Int): android.app.PendingIntent {
            return android.app.PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}