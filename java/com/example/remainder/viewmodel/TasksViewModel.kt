package com.example.remainder.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remainder.data.tasks.TaskEntity
import com.example.remainder.data.tasks.TasksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import com.example.remainder.data.tasks.TaskDao
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toSet
import java.time.LocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

class TasksViewModel(
    private val repository: TasksRepository
) : ViewModel() {

    fun getTaskById(id: Int) = repository
        .getTaskById(id)
        /**
        .stateIn(
            scope = viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            null
        )
*/
        val completedDates: StateFlow<Set<LocalDate>> =
            repository.completedDates // Flow<List<String>> z Dao
                .map { dateStrings ->
                    dateStrings.map {
                        LocalDate.parse(it)
                    }.toSet()
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())


    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val pendingTasks: StateFlow<List<TaskEntity>> = repository.pendingTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val doneTasks: StateFlow<List<TaskEntity>> = repository.doneTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.addTask(task)
            repository.scheduleTaskReminder(task)
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
            repository.scheduleTaskReminder(task)
        }
    }

    fun markTaskDone(task: TaskEntity, done: Boolean) {
        viewModelScope.launch {
            val now = if (done) System.currentTimeMillis() else null
            repository.updateTask(task.copy(isDone = done, completedTimestamp = now))
            if(done) {
                repository.cancelTaskReminder(task.id)
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.cancelTaskReminder(task.id)
            repository.deleteTask(task)
        }
    }

    val oneTimeTasks: StateFlow<List<TaskEntity>> = repository.oneTimeTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun completedTasksOn(dayStart: Long, dayEnd: Long): StateFlow<List<TaskEntity>> =
        repository.getCompletedTasksBetween(dayStart, dayEnd)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}