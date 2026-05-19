package com.example.todo.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.todo.repository.TaskRepository
import com.example.todo.data.TaskEntity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MARK_DONE = "com.example.todo.ACTION_MARK_DONE"
        const val EXTRA_TASK_ID = "taskId"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (ACTION_MARK_DONE == action) {
            val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
            if (taskId != -1) {
                // We need to mark the task as done and cancel its reminders.
                // We'll use the TaskRepository, but we need an instance.
                // Since we are in a BroadcastReceiver, we can get the application context.
                val appContext = context.applicationContext
                // We can get the repository from the application context if we store it there, or create a new one.
                // For simplicity, we'll create a new repository instance (it's lightweight).
                 val repository = TaskRepository(appContext)
                 // We need to run the suspend function in a coroutine. Use GlobalScope with Dispatchers.IO to avoid blocking the broadcast receiver.
                 GlobalScope.launch(Dispatchers.IO) {
                     val task = repository.getTaskById(taskId)
                     if (task != null) {
                         // Mark as done
                         val doneTask = task.copy(
                             status = com.example.todo.data.TaskStatus.DONE,
                             updatedAtMillis = System.currentTimeMillis()
                         )
                         repository.updateTask(doneTask)
                     }
                 }
            }
        }
    }
}