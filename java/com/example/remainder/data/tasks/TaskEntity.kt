package com.example.remainder.data.tasks

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import androidx.room.TypeConverter
import com.example.remainder.data.tasks.RepeatInterval

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // Podstawowe dane
    val listTitle: String,
    val tasksList: List<String>,

    // Termin wykonania
    val dueTimestamp: Long,

    // Powtarzalność
    val isRepeating: Boolean = false,
    val repeatInterval: RepeatInterval = RepeatInterval.NONE,
    val repeatEndTimestamp: Long? = null, // do kiedy powtarzać

    // Status
    val isDone: Boolean = false,

    val completedTimestamp: Long? = null,

    // Meta
    val createdAt: Long = System.currentTimeMillis(),
    val tasksDone: List<Boolean> = List(tasksList.size) { false }

)