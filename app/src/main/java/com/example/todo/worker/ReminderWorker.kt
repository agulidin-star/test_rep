package com.example.todo.worker

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todo.R
import com.example.todo.data.TaskDatabase
import com.example.todo.data.TaskEntity
import com.example.todo.data.TaskStatus
import com.example.todo.notification.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReminderWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val taskId = inputData.getLong("taskId", -1)
        val offsetMinutes = inputData.getInt("offsetMinutes", 0)

        if (taskId == -1) {
            return Result.failure()
        }

        // Get the task from the database
        val task = TaskDatabase.getInstance(context).taskDao().getById(taskId)
        if (task == null) {
            return Result.failure()
        }

        // If the task is already done, no need to notify
        if (task.status == TaskStatus.DONE) {
            return Result.success()
        }

        val now = System.currentTimeMillis()
        // If the due time has passed and the task is not done, we can show an overdue notification
        // or just let the periodic overdue worker handle marking it as overdue.
        // For the reminder worker, we are here because the offset time has arrived.
        // We'll show a reminder notification regardless of whether it's overdue or not.
        // But if it's overdue, we might want to change the message.

        val notificationHelper = NotificationHelper(context)
        val channelId = NotificationHelper.CHANNEL_ID_DEADLINES

        val title = task.title
        val content = if (now > task.dueDateTimeMillis) {
            "Дедлайн прошёл"
        } else {
            val minutesLeft = ((task.dueDateTimeMillis - now) / (60 * 1000)).toInt()
            "До дедлайна: $minutesLeft мин"
        }

        val intent = NotificationHelper.createMarkDoneIntent(context, taskId)
        val pendingIntent = NotificationHelper.createPendingIntent(context, intent, 0)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification) // You'll need to create this icon or use a default
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .addAction(R.drawable.ic_done, "Отметить выполнено", pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationHelper.notify(taskId.hashCode(), notification)

        return Result.success()
    }
}