package com.example.remainder.data.tasks

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    /** Wszystkie zadania, bez względu na stan */
    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<TaskEntity>>

    /** Tylko zadania nieoznaczone jako wykonane */
    @Query("SELECT * FROM tasks WHERE isDone = 0 and isRepeating = 0")
    fun getPendingTasks(): Flow<List<TaskEntity>>

    /** Tylko zadania oznaczone jako wykonane */
    @Query("SELECT * FROM tasks WHERE isDone = 1")
    fun getDoneTasks(): Flow<List<TaskEntity>>

    /** Dodaj nowe lub nadpisz istniejące (na podstawie klucza) */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    /** Zaktualizuj całe zadanie */
    @Update
    suspend fun update(task: TaskEntity)

    /** Usuń zadanie */
    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE isRepeating = 0")
    fun getOneTimeTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    fun getById(id: Int): Flow<TaskEntity?>

    @Query("""
    SELECT * FROM tasks
    WHERE completedTimestamp BETWEEN :dayStart AND :dayEnd
    ORDER BY completedTimestamp ASC
  """)
    fun getCompletedTasksBetween(dayStart: Long, dayEnd: Long): Flow<List<TaskEntity>>

    @Query("""
        SELECT DISTINCT date(completedTimestamp / 1000, 'unixepoch')
        FROM tasks
        WHERE completedTimestamp IS NOT NULL
    """)
    fun getCompletedDays(): Flow<List<String>>

}
