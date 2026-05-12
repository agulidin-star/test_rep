package com.example.todo.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todo.data.TaskDatabase
import com.example.todo.data.TaskEntity
import com.example.todo.data.TaskStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PeriodicOverdueWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val now = System.currentTimeMillis()
        val db = TaskDatabase.getInstance(context)
        val taskDao = db.taskDao()

        // Mark overdue tasks
        val updatedCount = taskDao.markOverdueTasks(now)
        // Update the widget after marking overdue
        updateWidget(context)

        return Result.success()
    }

    private fun updateWidget(context: Context) {
        // We'll use the same WidgetUpdateWorker as in the repository
        val workManager = androidx.work.WorkManager.getInstance(context)
        val updateRequest = androidx.work.OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
            .build()
        workManager.enqueueUniqueWork(
            "widget_update",
            androidx.work.ExistingWorkPolicy.REPLACE,
            updateRequest
        )
    }
}