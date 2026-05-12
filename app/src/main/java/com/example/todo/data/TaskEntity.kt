package com.example.todo.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String?,
    val dueDateTimeMillis: Long,
    val status: TaskStatus,
    val priority: TaskPriority,
    val remindersEnabled: Boolean,
    @TypeConverters(Converters::class)
    val reminderOffsetsMinutes: List<Int>,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

enum class TaskStatus {
    IN_PROGRESS,
    OVERDUE,
    DONE
}

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH
}