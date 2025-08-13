package com.example.remainder.data.tasks

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.remainder.TaskReminderReceiver
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TasksRepository(
    private val dao: TaskDao,
    private val context: Context
) {
    val allTasks: Flow<List<TaskEntity>> = dao.getAllTasks()
    val pendingTasks: Flow<List<TaskEntity>> = dao.getPendingTasks()
    val doneTasks: Flow<List<TaskEntity>> = dao.getDoneTasks()
    val oneTimeTasks: Flow<List<TaskEntity>> = dao.getOneTimeTasks()

    suspend fun addTask(task: TaskEntity) {
        dao.insert(task)
    }

    suspend fun updateTask(task: TaskEntity) {
        dao.update(task)
    }

    suspend fun deleteTask(task: TaskEntity) {
        dao.delete(task)
    }

    fun getCompletedTasksBetween(dayStart: Long, dayEnd: Long): Flow<List<TaskEntity>> =
        dao.getCompletedTasksBetween(dayStart, dayEnd)

    val completedDates: Flow<List<String>> = dao.getCompletedDays()

    fun getTaskById(id: Int): Flow<TaskEntity?> = dao.getById(id)

    fun scheduleTaskReminder(task: TaskEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra("extra_list_title", task.listTitle)
            putExtra("extra_is_repeating", task.isRepeating)
            putExtra("extra_task_id", task.id)
            putExtra("extra_repeat_interval", task.repeatInterval.name)
            putExtra("extra_timestamp", task.dueTimestamp)
            putExtra("extra_repeat_end", task.repeatEndTimestamp ?: -1L) // -1 brak końca
            val timeText = SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(Date(task.dueTimestamp))
            putExtra("extra_time_text", timeText)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            task.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Brak uprawnienia do dokładnych alarmów – musisz poprosić użytkownika")
            }
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                task.dueTimestamp,
                pi
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Nie udało się zarezerwować alarmu (SecurityException)", e)
        }
    }

    fun cancelTaskReminder(taskId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pi != null) {
            try {
                alarmManager.cancel(pi)
                pi.cancel()
            } catch (e: SecurityException) {
                Log.e(TAG, "Nie udało się anulować alarmu (SecurityException)", e)
            }
        }
    }
}
