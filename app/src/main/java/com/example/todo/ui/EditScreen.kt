package com.example.todo.ui

import android.widget.DatePicker
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleSelectionSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.todo.data.TaskEntity
import com.example.todo.data.TaskPriority
import com.example.todo.data.TaskStatus
import com.example.todo.repository.TaskRepository
import com.example.todo.ui.theme.SteelBlue
import com.example.todo.ui.theme.SteelGray
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EditScreen(
    repository: TaskRepository,
    taskId: Long?,
    onNavigateUp: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var title       by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueMillis   by remember { mutableStateOf(System.currentTimeMillis()) }
    var priority    by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var status      by remember { mutableStateOf(TaskStatus.IN_PROGRESS) }
    var remEnabled  by remember { mutableStateOf(true) }
    val offsets     = remember { mutableStateListOf(1440, 60, 15) }

    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }

    val isNew    = taskId == null || taskId == 0L
    val dateFmt = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    // ── Load existing task ────────────────────────────────────────────────────
    LaunchedEffect(taskId) {
        taskId?.let { id ->
            if (id != 0L) repository.getTaskById(id)?.let { t ->
                title       = t.title
                description = t.description ?: ""
                dueMillis   = t.dueDateTimeMillis
                priority    = t.priority
                status      = t.status
                remEnabled  = t.remindersEnabled
                offsets.clear()
                offsets.addAll(t.reminderOffsetsMinutes)
            }
        }
    }

    // ── Layout ────────────────────────────────────────────────────────────────
    androidx.compose.material3.Scaffold(
        topBar = {
            SmallTopAppBar(
                title         = { Text(if (isNew) "Новая задача" else "Редактировать", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад", tint = SteelBlue)
                    }
                },
                actions       = {
                    TextButton(onClick = {
                        scope.launch {
                            val entity = TaskEntity(
                                id                   = taskId ?: 0,
                                title                = title,
                                description          = description.ifBlank { null },
                                dueDateTimeMillis    = dueMillis,
                                status               = status,
                                priority             = priority,
                                remindersEnabled     = remEnabled,
                                reminderOffsetsMinutes = offsets.toList(),
                                createdAtMillis      = System.currentTimeMillis(),
                                updatedAtMillis      = System.currentTimeMillis()
                            )
                            if (isNew) repository.insertTask(entity) else repository.updateTask(entity)
                            onNavigateUp()
                        }
                    }) { Text("Сохранить", color = SteelBlue) }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            if (!isNew) {
                androidx.compose.material3.FloatingActionButton(
                    onClick = {
                        scope.launch {
                            repository.getTaskById(taskId!!)?.let { repository.deleteTask(it) }
                            onNavigateUp()
                        }
                    },
                    containerColor = Color(0xFFB3261E),
                    contentColor   = Color.White,
                    modifier       = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Удалить")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Title
            OutlinedTextField(
                value       = title,
                onValueChange = { title = it },
                label       = { Text("Название") },
                singleLine  = true,
                modifier    = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Description
            OutlinedTextField(
                value       = description,
                onValueChange = { description = it },
                label       = { Text("Описание") },
                modifier    = Modifier.fillMaxWidth(),
                minLines    = 3
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Due date/time
            if (showDate) {
                val dpState = rememberDatePickerState(initialSelectedDateMillis = dueMillis)
                DatePickerDialog(
                    onDismissRequest  = { showDate = false },
                    confirmButton     = {
                        TextButton(onClick = {
                            dpState.selectedDateMillis?.let { raw ->
                                val c = java.util.Calendar.getInstance().apply { timeInMillis = raw }
                                java.util.Calendar.getInstance().apply { timeInMillis = dueMillis }.let { cur ->
                                    c.set(java.util.Calendar.HOUR_OF_DAY, cur.get(java.util.Calendar.HOUR_OF_DAY))
                                    c.set(java.util.Calendar.MINUTE, cur.get(java.util.Calendar.MINUTE))
                                }
                                dueMillis = c.timeInMillis
                            }
                            showDate = false
                        }) { Text("OK") }
                    },
                    dismissButton    = {
                        TextButton(onClick = { showDate = false }) { Text("Отмена") }
                    }
                ) { DatePicker(state = dpState) }
            }

            if (showTime) {
                val cNow = java.util.Calendar.getInstance().apply { timeInMillis = dueMillis }
                val tpState = rememberTimePickerState(
                    initialHour   = cNow.get(java.util.Calendar.HOUR_OF_DAY),
                    initialMinute = cNow.get(java.util.Calendar.MINUTE)
                )
                DatePickerDialog(
                    onDismissRequest = { showTime = false },
                    confirmButton    = {
                        TextButton(onClick = {
                            val c = java.util.Calendar.getInstance().apply { timeInMillis = dueMillis }
                            c.set(java.util.Calendar.HOUR_OF_DAY, tpState.hour)
                            c.set(java.util.Calendar.MINUTE, tpState.minute)
                            dueMillis = c.timeInMillis
                            showTime = false
                        }) { Text("OK") }
                    },
                    dismissButton   = {
                        TextButton(onClick = { showTime = false }) { Text("Отмена") }
                    }
                ) { TimePicker(state = tpState) }
            }

            OutlinedTextField(
                value     = "${dateFmt.format(Date(dueMillis))}  ${timeFmt.format(Date(dueMillis))}",
                onValueChange = {},
                label     = { Text("Дата и время дедлайна") },
                readOnly  = true,
                modifier  = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Row {
                        IconButton(onClick = { showDate = !showDate }) {
                            Icon(Icons.Filled.DateRange, contentDescription = "Дата", tint = SteelBlue)
                        }
                        IconButton(onClick = { showTime = !showTime }) {
                            Icon(Icons.Filled.Schedule, contentDescription = "Время", tint = SteelBlue)
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Priority
            Text("Приоритет", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            PrioritySection(priority = priority, onSelect = { priority = it })
            Spacer(modifier = Modifier.height(16.dp))

            // Status
            Text("Статус", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            StatusSection(status = status, onSelect = { status = it })
            Spacer(modifier = Modifier.height(16.dp))

            // Reminders
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Напоминания", modifier = Modifier.weight(1f))
                Switch(checked = remEnabled, onCheckedChange = { remEnabled = it })
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (remEnabled) {
                OffsetSection(offsets = offsets)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (!isNew) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        scope.launch {
                            repository.getTaskById(taskId!!)?.let { repository.deleteTask(it) }
                            onNavigateUp()
                        }
                    },
                    colors    = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E)),
                    modifier  = Modifier.fillMaxWidth()
                ) { Text("Удалить задачу", color = Color.White) }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/* ── Segments ───────────────────────────────────────────────────────────────── */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrioritySection(priority: TaskPriority, onSelect: (TaskPriority) -> Unit) {
    val opts = listOf(
        "Низкий"  to TaskPriority.LOW,
        "Средний" to TaskPriority.MEDIUM,
        "Высокий" to TaskPriority.HIGH
    )
    SingleSelectionSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        opts.forEachIndexed { idx, (_, prio) ->
            SegmentedButton(
                selected = priority == prio,
                onClick  = { onSelect(prio) },
                shape    = SegmentedButtonDefaults.itemShape(idx, opts.size),
                colors   = SegmentedButtonDefaults.colors(
                    activeContainerColor   = SteelBlue,
                    activeContentColor     = Color.White,
                    inactiveContainerColor = SteelGray,
                    inactiveContentColor   = SteelGray
                )
            ) {
                Text(prioLabel(prio))
            }
        }
    }
}

@Composable
private fun prioLabel(p: TaskPriority) = when (p) {
    TaskPriority.LOW    -> "Низкий"
    TaskPriority.MEDIUM -> "Средний"
    TaskPriority.HIGH   -> "Высокий"
}

@Composable
private fun StatusSection(status: TaskStatus, onSelect: (TaskStatus) -> Unit) {
    val opts = listOf(TaskStatus.IN_PROGRESS, TaskStatus.DONE)
    SingleSelectionSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        opts.forEachIndexed { idx, st ->
            SegmentedButton(
                selected = status == st,
                onClick  = { onSelect(st) },
                shape    = SegmentedButtonDefaults.itemShape(idx, opts.size),
                colors   = SegmentedButtonDefaults.colors(
                    activeContainerColor   = SteelBlue,
                    activeContentColor     = Color.White,
                    inactiveContainerColor = SteelGray,
                    inactiveContentColor   = SteelGray
                )
            ) {
                Text(if (st == TaskStatus.IN_PROGRESS) "В работе" else "Выполнено")
            }
        }
    }
}

/* ── Reminder offsets ───────────────────────────────────────────────────────── */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OffsetSection(offsets: MutableList<Int>) {
    var dropdownOpen by remember { mutableStateOf(false) }
    val quick = listOf(15, 30, 60, 180, 360, 720, 1440)

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Нейро (мин):", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        ExposedDropdownMenuBox(
            expanded = dropdownOpen,
            onExpandedChange = { dropdownOpen = !dropdownOpen },
            modifier = Modifier.width(130.dp)
        ) {
            OutlinedTextField(
                value     = "+", onValueChange = {},
                readOnly  = true,
                label     = { Text("Добавить") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownOpen) },
                modifier  = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(expanded = dropdownOpen, onDismissRequest = { dropdownOpen = false }) {
                quick.forEach { m ->
                    val lbl = offsetLabel(m)
                    DropdownMenuItem(onClick = {
                        if (m !in offsets) offsets.add(m)
                        dropdownOpen = false
                    }, text = { Text(lbl) })
                }
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    offsets.chunked(3).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            row.forEach { mins ->
                Button(
                    onClick = { offsets.remove(mins) },
                    colors  = ButtonDefaults.buttonColors(containerColor = SteelGray),
                    shape   = RoundedCornerShape(8.dp),
                    modifier = Modifier.width(100.dp)
                ) { Text("${offsetLabel(mins)} ✕", color = Color.White) }
            }
            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
        }
        Spacer(Modifier.height(4.dp))
    }
}

private fun offsetLabel(mins: Int): String = when {
    mins >= 1440 && mins % 1440 == 0 -> "${mins / 1440} д"
    mins >= 60   && mins % 60   == 0 -> "${mins / 60} ч"
    else                              -> "$mins мин"
}
