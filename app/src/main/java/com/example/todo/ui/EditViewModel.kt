package com.example.todo.ui

import androidx.lifecycle.*
import com.example.todo.data.TaskEntity
import com.example.todo.data.TaskPriority
import com.example.todo.data.TaskStatus
import com.example.todo.repository.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditViewModel(
    private val repository: TaskRepository,
    private val taskId: Long? // null for new task
) : ViewModel() {

    // UI State for the form
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _dueDateTimeMillis = MutableStateFlow(System.currentTimeMillis())
    val dueDateTimeMillis: StateFlow<Long> = _dueDateTimeMillis.asStateFlow()

    private val _priority = MutableStateFlow(TaskPriority.MEDIUM)
    val priority: StateFlow<TaskPriority> = _priority.asStateFlow()

    private val _status = MutableStateFlow(TaskStatus.IN_PROGRESS)
    val status: StateFlow<TaskStatus> = _status.asStateFlow()

    private val _remindersEnabled = MutableStateFlow(true)
    val remindersEnabled: StateFlow<Boolean> = _remindersEnabled.asStateFlow()

    private val _reminderOffsetsMinutes = MutableStateFlow(listOf(1440, 60, 15)) // 24h, 1h, 15m
    val reminderOffsetsMinutes: StateFlow<List<Int>> = _reminderOffsetsMinutes.asStateFlow()

    // For UI: we might want to show a loading state or error, but for simplicity we skip.

    // Load existing task if taskId is provided
    init {
        viewModelScope.launch {
            taskId?.let { id ->
                val task = repository.getTaskById(id)
                if (task != null) {
                    _title.value = task.title
                    _description.value = task.description ?: ""
                    _dueDateTimeMillis.value = task.dueDateTimeMillis
                    _priority.value = task.priority
                    _status.value = task.status
                    _remindersEnabled.value = task.remindersEnabled
                    _reminderOffsetsMinutes.value = task.reminderOffsetsMinutes
                }
            }
        }
    }

    // Actions
    fun setTitle(title: String) {
        _title.value = title
    }

    fun setDescription(description: String) {
        _description.value = description
    }

    fun setDueDateTimeMillis(millis: Long) {
        _dueDateTimeMillis.value = millis
    }

    fun setPriority(priority: TaskPriority) {
        _priority.value = priority
    }

    fun setStatus(status: TaskStatus) {
        _status.value = status
    }

    fun setRemindersEnabled(enabled: Boolean) {
        _remindersEnabled.value = enabled
    }

    fun setReminderOffsetsMinutes(offsets: List<Int>) {
        _reminderOffsetsMinutes.value = offsets
    }

    fun saveTask() = viewModelScope.launch {
        val task = TaskEntity(
            id = taskId ?: 0, // if new, id will be ignored and auto-generated
            title = _title.value,
            description = if (_description.value.isBlank()) null else _description.value,
            dueDateTimeMillis = _dueDateTimeMillis.value,
            status = _status.value,
            priority = _priority.value,
            remindersEnabled = _remindersEnabled.value,
            reminderOffsetsMinutes = _reminderOffsetsMinutes.value,
            createdAtMillis = if (taskId == null) System.currentTimeMillis() else 0, // will be set in repository
            updatedAtMillis = if (taskId == null) System.currentTimeMillis() else 0
        )
        if (taskId == null) {
            repository.insertTask(task)
        } else {
            repository.updateTask(task)
        }
    }

    fun deleteTask() = viewModelScope.launch {
        taskId?.let { id ->
            val task = repository.getTaskById(id)
            task?.let { repository.deleteTask(it) }
        }
    }
}