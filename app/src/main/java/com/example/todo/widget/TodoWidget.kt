package com.example.todo.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.layout.weight as glanceWeight
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.Color
import com.example.todo.data.TaskDatabase
import com.example.todo.data.TaskEntity
import com.example.todo.data.TaskPriority
import com.example.todo.data.TaskStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class TodoWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val tasks = withContext(Dispatchers.IO) {
            try {
                TaskDatabase.getInstance(context).taskDao().getAll().firstOrNull() ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
        provideContent { WidgetContent(tasks) }
    }

    companion object {
        suspend fun update(context: Context) { updateAll(context) }
    }
}

// ── Colours ──────────────────────────────────────────────────────────────────
private val SteelBlue = Color(0xFF5A6D7E)
private val SteelGray = Color(0xFF8A9BA8)
private val SteelDark = Color(0xFF1C222B)
private val RedBg     = Color(0xFFB3261E)
private val GreenBg   = Color(0xFF2E7D52)

// ── Pixel helpers: Flat pixel helpers for Glance (uses float, not Compose Dp) ──
private const val P16 = 16f
private const val P12 = 12f
private const val P8  = 8f
private const val P4  = 4f

@Composable
private fun WidgetContent(tasks: List<TaskEntity>) {
    val nextTasks = tasks
        .filter { it.status != TaskStatus.DONE }
        .sortedBy { it.dueDateTimeMillis }
        .take(5)

    Column(
        modifier = GlanceModifier.fillMaxSize().padding(P16)
    ) {
        Text("To-Do", style = TextStyle(
            color = SteelBlue, fontSize = 28f, fontWeight = FontWeight.Bold))
        val today = java.time.LocalDate.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy"))
        Text(today, style = TextStyle(color = SteelGray, fontSize = 14f),
            modifier = GlanceModifier.padding(top = P4))
        Spacer(GlanceModifier.height(12f))

        if (nextTasks.isEmpty()) {
            Text("Нет активных задач",
                style = TextStyle(color = SteelGray, fontSize = 16f),
                modifier = GlanceModifier.padding(vertical = 8f))
        } else {
            nextTasks.forEachIndexed { idx, task ->
                TaskRow(task)
                if (idx != nextTasks.lastIndex) Spacer(GlanceModifier.height(8f))
            }
        }
    }
}

@Composable
private fun TaskRow(task: TaskEntity) {
    val pColor = when (task.priority) {
        TaskPriority.HIGH   -> RedBg
        TaskPriority.MEDIUM -> Color(0xFFF57C00)
        TaskPriority.LOW    -> GreenBg
    }
    val sColor = when (task.status) {
        TaskStatus.OVERDUE     -> RedBg
        TaskStatus.IN_PROGRESS -> SteelBlue
        TaskStatus.DONE        -> GreenBg
    }
    val dueLabel = if (task.dueDateTimeMillis <= System.currentTimeMillis()) {
        "Просрочено"
    } else {
        val diffMins = (task.dueDateTimeMillis - System.currentTimeMillis()) / (1000 * 60)
        when {
            diffMins >= 1440  -> "${diffMins / 1440} д"
            diffMins >= 60    -> "${diffMins / 60} ч"
            else              -> "${diffMins % 60} мин"
        }
    }

    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(48f)
    ) {
        Row(
            verticalAlignment = Alignment.VerticalCenter,
            modifier = GlanceModifier.fillMaxSize()
        ) {
            Column(modifier = GlanceModifier.glanceWeight(1f)) {
                Text(task.title,
                    style = TextStyle(
                        color = SteelDark, fontSize = 16f, fontWeight = FontWeight.Medium))
                Text(dueLabel, style = TextStyle(color = sColor, fontSize = 12f))
            }
            Spacer(GlanceModifier.glanceWeight(1f))
            androidx.glance.layout.Box(
                modifier = GlanceModifier.padding(start = P4).size(P4)
            )
            Spacer(GlanceModifier.width(P8))
        }
    }
}
