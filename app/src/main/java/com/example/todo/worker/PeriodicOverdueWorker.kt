package com.example.todo.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todo.data.TaskDatabase
import com.example.todo.data.TaskStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PeriodicOverdueWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val db = TaskDatabase.getInstance(applicationContext)
            val dao = db.taskDao()
            val now = System.currentTimeMillis()

            // Mark overdue tasks
            dao.markOverdueTasks(now)

            // Update widget
            androidx.glance.appwidget.GlanceAppWidget.updateAll(
                applicationContext,
                com.example.todo.widget.TodoWidget::class.java
            )
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
