package com.example.todo.repository

import android.app.Application
import android.content.Context
import androidx.work.*;
import com.example.todo.data.TaskDatabase
import com.example.todo.data.TaskEntity
import com.example.todo.data.TaskStatus
import com.example.todo.data.TaskPriority
import com.example.todo.worker.ReminderWorker
import com.example.todo.worker.PeriodicOverdueWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*

class TaskRepository(private val application: Application) {

    private val taskDao = TaskDatabase.getInstance(application).taskDao()
    private val workManager = WorkManager.getInstance(application)

    // For UI: Flow of all tasks
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAll()

    // For UI: Flow of tasks by status
    fun getTasksByStatus(status: TaskStatus): Flow<List<TaskEntity>> {
        return taskDao.getByStatus(status)
    }

    // Insert a task and schedule its reminders
    suspend fun insertTask(task: TaskEntity) {
        val taskWithId = TaskEntity(
            id = task.id,
            title = task.title,
            description = task.description,
            dueDateTimeMillis = task.dueDateTimeMillis,
            status = task.status,
            priority = task.priority,
            remindersEnabled = task.remindersEnabled,
            reminderOffsetsMinutes = task.reminderOffsetsMinutes,
            createdAtMillis = task.createdAtMillis,
            updatedAtMillis = System.currentTimeMillis()
        )
        taskDao.insert(taskWithId)
        if (taskWithId.remindersEnabled) {
            scheduleReminders(taskWithId)
        }
        // Update widget after DB change
        updateWidget()
    }

    // Update a task and reschedule its reminders
    suspend fun updateTask(task: TaskEntity) {
        val updatedTask = TaskEntity(
            id = task.id,
            title = task.title,
            description = task.description,
            dueDateTimeMillis = task.dueDateTimeMillis,
            status = task.status,
            priority = task.priority,
            remindersEnabled = task.remindersEnabled,
            reminderOffsetsMinutes = task.reminderOffsetsMinutes,
            createdAtMillis = task.createdAtMillis,
            updatedAtMillis = System.currentTimeMillis()
        )
        taskDao.update(updatedTask)
        // Cancel existing work for this task
        cancelReminders(task.id)
        // Schedule new reminders if enabled
        if (updatedTask.remindersEnabled) {
            scheduleReminders(updatedTask)
        }
        // Update widget after DB change
        updateWidget()
    }

    // Delete a task and cancel its reminders
    suspend fun deleteTask(task: TaskEntity) {
        taskDao.delete(task)
        cancelReminders(task.id)
        // Update widget after DB change
        updateWidget()
    }

    // Get a task by ID
    suspend fun getTaskById(id: Long): TaskEntity? {
        return taskDao.getById(id)
    }

    // Schedule reminders for a task using WorkManager
    private fun scheduleReminders(task: TaskEntity) {
        val now = System.currentTimeMillis()
        if (task.dueDateTimeMillis <= now) {
            // If due time is in the past, don't schedule future reminders
            // But we might want to show an overdue notification immediately?
            // For simplicity, we'll rely on the periodic overdue worker to mark it overdue
            return
        }

        for (offset in task.reminderOffsetsMinutes) {
            val triggerTime = task.dueDateTimeMillis - (offset * 60 * 1000) // Convert minutes to milliseconds
            if (triggerTime > now) {
                val delay = triggerTime - now
                val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .addTag("reminder_${task.id}_$offset")
                    .setInputData(
                        workDataOf(
                            "taskId" to task.id,
                            "offsetMinutes" to offset
                        )
                    )
                    .build()
                workManager.enqueueUniqueWork(
                    "reminder_${task.id}_$offset",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
            }
        }
    }

    // Cancel all reminder work for a task
    fun cancelReminders(taskId: Long) {
        workManager.cancelAllWorkByTag("reminder_${taskId}_*")
    }

    // Schedule periodic worker to mark overdue tasks and update widget
    fun schedulePeriodicOverdueWorker() {
        val periodicWorkRequest = PeriodicWorkRequestBuilder<PeriodicOverdueWorker>(60, TimeUnit.MINUTES)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "periodic_overdue_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }

    // Cancel periodic overdue worker (e.g., on app clear data)
    fun cancelPeriodicOverdueWorker() {
        workManager.cancelUniqueWork("periodic_overdue_worker")
    }

    // Update the Glance widget (simplified: we'll trigger an update via WorkManager or direct call)
    // For simplicity, we'll use a WorkManager worker to update the widget, but we can also call updateAll directly if we have Context.
    // Since we might not always have a UI context, using WorkManager is safer for background updates.
    // However, for immediate updates, we can use a method that requires Context.
    // Let's create a method that uses the application context to update the widget.
    fun updateWidget() {
        val context = application.applicationContext
        // In a real app, we might use GlanceAppWidget.updateAll(context, TodoWidget::class)
        // But for simplicity, we'll rely on the periodic worker or manual trigger.
        // We'll create a one-time work request to update the widget.
        val updateRequest = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
            .build()
        workManager.enqueueUniqueWork(
            "widget_update",
            ExistingWorkPolicy.REPLACE,
            updateRequest
        )
    }
}

// Worker to update the Glance widget
class WidgetUpdateWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        try {
            // Update all instances of the widget
            androidx.glance.appwidget.GlanceAppWidget.updateAll(
                applicationContext,
                com.example.todo.widget.TodoWidget::class.java
            )
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}