package com.example.todo.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getById(taskId: Long): TaskEntity?

    @Query("SELECT * FROM tasks ORDER BY dueDateTimeMillis ASC")
    fun getAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY dueDateTimeMillis ASC")
    fun getByStatus(status: TaskStatus): Flow<List<TaskEntity>>

    // Mark overdue tasks: set status to OVERDUE when dueDateTime passed and status is not DONE
    @Query("UPDATE tasks SET status = 'OVERDUE' WHERE dueDateTimeMillis < :now AND status != 'DONE'")
    fun markOverdueTasks(now: Long): Int
}