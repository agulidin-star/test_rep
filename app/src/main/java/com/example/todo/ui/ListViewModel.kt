package com.example.todo.ui

import androidx.lifecycle.*
import com.example.todo.data.TaskEntity
import com.example.todo.data.TaskPriority
import com.example.todo.data.TaskStatus
import com.example.todo.repository.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ListViewModel(private val repository: TaskRepository) : ViewModel() {

    // UI State
    val tasks = repository.allTasks
        .map { list ->
            list.sortedBy { it.dueDateTimeMillis }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // Filter and sort options
    private val _filterStatus = MutableStateFlow<TaskStatus?>(null)
    val filterStatus: StateFlow<TaskStatus?> = _filterStatus.asStateFlow()

    private val _sortByPriority = MutableStateFlow(false)
    val sortByPriority: StateFlow<Boolean> = _sortByPriority.asStateFlow()

    // Filtered and sorted tasks
    val filteredTasks: StateFlow<List<TaskEntity>> = combine(
        tasks,
        _filterStatus,
        _sortByPriority
    ) { list, status, sortByPriority ->
        var filtered = list
        if (status != null) {
            filtered = filtered.filter { it.status == status }
        }
        if (sortByPriority) {
            filtered = filtered.sortedBy { it.priority }
                .thenBy { it.dueDateTimeMillis }
        } else {
            filtered = filtered.sortedBy { it.dueDateTimeMillis }
        }
        filtered
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // Actions
    fun setFilterStatus(status: TaskStatus?) {
        _filterStatus.value = status
    }

    fun setSortByPriority(isSortByPriority: Boolean) {
        _sortByPriority.value = isSortByPriority
    }

    fun addTask(task: TaskEntity) = viewModelScope.launch {
        repository.insertTask(task)
    }

    fun updateTask(task: TaskEntity) = viewModelScope.launch {
        repository.updateTask(task)
    }

    fun deleteTask(task: TaskEntity) = viewModelScope.launch {
        repository.deleteTask(task)
    }

    // Initialize periodic worker when ViewModel is created
    init {
        viewModelScope.launch {
            repository.schedulePeriodicOverdueWorker()
        }
    }
}