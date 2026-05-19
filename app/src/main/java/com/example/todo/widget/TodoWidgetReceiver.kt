package com.example.todo.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.todo.MainActivity
import com.example.todo.R
import com.example.todo.data.TaskDatabase
import com.example.todo.data.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class TodoWidgetReceiver : AppWidgetProvider() {

    private var updateJob: Job? = null

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateJob?.cancel()
        updateJob = CoroutineScope(Dispatchers.IO).launch {
            val tasks = runCatching {
                TaskDatabase.getInstance(context).taskDao().getAll().firstOrNull() ?: emptyList()
            }.getOrDefault(emptyList())

            val activeTasks = tasks
                .filter { it.status.name != "DONE" }
                .sortedBy { it.dueDateTimeMillis }
                .take(5)

            appWidgetIds.forEach { widgetId ->
                val views = RemoteViews(context.packageName, R.layout.widget_todo).apply {
                    setTextViewText(R.id.widget_date,
                        activeTasks.firstOrNull()?.title ?: context.getString(R.string.add_task)
                    )

                    val appIntent = android.content.Intent(context, MainActivity::class.java)
                    val pi = android.app.PendingIntent.getActivity(
                        context, 0, appIntent,
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT or
                                android.app.PendingIntent.FLAG_IMMUTABLE
                    )
                    setOnClickPendingIntent(R.id.widget_root, pi)

                    val addIntent = android.content.Intent(context, MainActivity::class.java)
                    val addPi = android.app.PendingIntent.getActivity(
                        context, 1, addIntent,
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT or
                                android.app.PendingIntent.FLAG_IMMUTABLE
                    )
                    setOnClickPendingIntent(R.id.widget_add_btn, addPi)
                }
                appWidgetManager.updateAppWidget(widgetId, views)
            }

            // Refresh Glance widget components that might also be registered.
            try {
                androidx.glance.appwidget.GlanceAppWidget.updateAll(context, TodoWidget::class.java)
            } catch (_: Exception) { /* Glance widget not registered, ignore */ }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        val ids = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(android.content.ComponentName(context, TodoWidgetReceiver::class.java))
        onUpdate(context, AppWidgetManager.getInstance(context), ids)
    }

    override fun onDisabled(context: Context) {
        updateJob?.cancel()
        updateJob = null
        super.onDisabled(context)
    }

    companion object {
        suspend fun getActiveTasks(context: Context): List<TaskEntity> = runCatching {
            TaskDatabase.getInstance(context).taskDao().getAll().firstOrNull() ?: emptyList()
        }.getOrDefault(emptyList())
    }
}
