package com.example.todo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleSelectionSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.todo.data.TaskEntity
import com.example.todo.data.TaskPriority
import com.example.todo.data.TaskStatus
import com.example.todo.repository.TaskRepository
import com.example.todo.ui.theme.Blue40
import com.example.todo.ui.theme.SteelBlue
import com.example.todo.ui.theme.SteelGray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ListScreen(
    repository: TaskRepository,
    onOpenTask: (Long) -> Unit,
    onNewTask: () -> Unit
) {
    val tasks   by repository.allTasks.collectAsState(initial = emptyList())
    val descFmt = rememberDateFormat()

    var filterStatus   by remember { mutableStateOf<TaskStatus?>(null) }
    var sortByPriority by remember { mutableStateOf(false) }

    val availableStatuses = listOf(
        null              to "Все",
        TaskStatus.IN_PROGRESS to "В работе",
        TaskStatus.OVERDUE     to "Просрочено",
        TaskStatus.DONE        to "Выполнено"
    )

    val filtered = tasks
        .filter { filterStatus == null || it.status == filterStatus }
        .sortedWith(priorityComparator(sortByPriority))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Задачи", fontWeight = FontWeight.SemiBold) },
                actions = {
                    SortToggle(sortByPriority = sortByPriority,
                        onToggle = { sortByPriority = it })
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = onNewTask,
                containerColor = SteelBlue,
                contentColor   = Color.White,
                modifier       = Modifier.size(56.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
        ) {
            // Status filter chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableStatuses.forEach { (status, label) ->
                    FilterChip(
                        selected       = filterStatus == status,
                        onClick        = { filterStatus = if (filterStatus == status) null else status },
                        label          = { Text(label) },
                        colors         = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SteelBlue,
                            selectedLabelColor     = Color.White
                        ),
                        modifier       = Modifier.weight(1f)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            // Task list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding      = PaddingValues(vertical = 8.dp)
            ) {
                items(filtered, key = { it.id }) { task ->
                    TaskRow(
                        task       = task,
                        onOpen     = { onOpenTask(task.id) },
                        onDone     = {
                            repository.updateTask(task.copy(
                                status           = TaskStatus.DONE,
                                updatedAtMillis  = System.currentTimeMillis()
                            ))
                        },
                        onDelete   = { repository.deleteTask(task) }
                    )
                }
                if (filtered.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Нет задач",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Blue40)
                        }
                    }
                }
            }
        }
    }
}

// ── Sort toggle ───────────────────────────────────────────────────────────────

@Composable
private fun SortToggle(sortByPriority: Boolean, onToggle: (Boolean) -> Unit) {
    SingleSelectionSegmentedButtonRow(modifier = Modifier.padding(end = 8.dp)) {
        SegmentedButton(
            selected = !sortByPriority,
            onClick  = { onToggle(false) },
            shape    = SegmentedButtonDefaults.itemShape(0, 2),
            colors   = SegmentedButtonDefaults.colors(
                activeContainerColor   = SteelGray,
                activeContentColor     = SteelGray,
                inactiveContainerColor = SteelGray,
                inactiveContentColor   = SteelGray
            )
        ) { Text("По дедлайну") }
        SegmentedButton(
            selected = sortByPriority,
            onClick  = { onToggle(true) },
            shape    = SegmentedButtonDefaults.itemShape(0, 2),
            colors   = SegmentedButtonDefaults.colors(
                activeContainerColor   = SteelGray,
                activeContentColor     = SteelGray,
                inactiveContainerColor = SteelGray,
                inactiveContentColor   = SteelGray
            )
        ) { Text("По приоритету") }
    }
}

// ── Task row ──────────────────────────────────────────────────────────────────

@Composable
private fun TaskRow(
    task: TaskEntity,
    onOpen: () -> Unit,
    onDone: () -> Unit,
    onDelete: () -> Unit
) {
    val dueStr  = descFmt.format(Date(task.dueDateTimeMillis))
    val statusIcon = when (task.status) {
        TaskStatus.OVERDUE     -> Icons.Filled.Warning
        TaskStatus.IN_PROGRESS -> Icons.Filled.PlayArrow
        TaskStatus.DONE        -> Icons.Filled.CheckCircle
    }
    val statusColor    = statusColor(task.status)
    val priorityColor  = when (task.priority) {
        TaskPriority.HIGH   -> Color(0xFFC62828)
        TaskPriority.MEDIUM -> Color(0xFFF57C00)
        TaskPriority.LOW    -> Color(0xFF2E7D52)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF0F4F8))
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text         = task.title,
                        style        = MaterialTheme.typography.titleMedium,
                        fontWeight   = FontWeight.SemiBold,
                        maxLines     = 2,
                        overflow     = TextOverflow.Ellipsis
                    )
                    if (!task.description.isNullOrBlank()) {
                        Text(
                            text       = task.description!!,
                            style      = MaterialTheme.typography.bodySmall,
                            color      = Blue40,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis,
                            modifier   = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                // Priority bar
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(width = 6.dp, height = 36.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(priorityColor)
                )
            }

            Row(
                modifier       = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector    = statusIcon,
                    contentDescription = null,
                    tint           = statusColor,
                    modifier       = Modifier.size(16.dp)
                )
                Text(
                    text   = dueStr,
                    style  = MaterialTheme.typography.bodySmall,
                    color  = statusColor,
                    modifier = Modifier.padding(start = 4.dp)
                )
                if (task.remindersEnabled) {
                    Icon(
                        imageVector    = Icons.Filled.Notifications,
                        contentDescription = "Напоминания",
                        tint           = SteelGray,
                        modifier       = Modifier.padding(start = 12.dp).size(16.dp)
                    )
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            IconButton(
                onClick   = onDone,
                enabled   = task.status != TaskStatus.DONE
            ) {
                Icon(
                    imageVector    = Icons.Filled.CheckCircle,
                    contentDescription = "Выполнено",
                    tint           = if (task.status == TaskStatus.DONE) Color(0xFF2E7D52) else Blue40
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector    = Icons.Filled.Delete,
                    contentDescription = "Удалить",
                    tint           = Color(0xFFB3261E)
                )
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun priorityComparator(sortByPriority: Boolean): Comparator<TaskEntity> {
    val order = mapOf(TaskPriority.HIGH to 0, TaskPriority.MEDIUM to 1, TaskPriority.LOW to 2)
    return if (sortByPriority) {
        compareBy<TaskEntity> { order[it.priority] ?: 3 }
            .thenBy { it.dueDateTimeMillis }
    } else {
        compareBy { it.dueDateTimeMillis }
    }
}

private fun statusColor(s: TaskStatus): Color = when (s) {
    TaskStatus.OVERDUE     -> Color(0xFFB3261E)
    TaskStatus.IN_PROGRESS -> SteelBlue
    TaskStatus.DONE        -> Color(0xFF2E7D52)
}

@Composable
private fun rememberDateFormat(): SimpleDateFormat = remember {
    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
}
