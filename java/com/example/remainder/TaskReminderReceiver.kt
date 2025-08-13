package com.example.remainder

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.remainder.data.tasks.RepeatInterval
import java.util.Calendar
import java.util.concurrent.TimeUnit

class TaskReminderReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        val taskId       = intent.getIntExtra("extra_task_id", -1)
        val listTitle    = intent.getStringExtra("extra_list_title") ?: "Task"
        val timeText     = intent.getStringExtra("extra_time_text") ?: ""
        val timestamp    = intent.getLongExtra("extra_timestamp", 0L)
        val isRepeating  = intent.getBooleanExtra("extra_is_repeating", false)
        val intervalName = intent.getStringExtra("extra_repeat_interval") ?: RepeatInterval.NONE.name
        val repeatEnd    = intent.getLongExtra("extra_repeat_end", -1L)

        // Wyświetlenie powiadomienia
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentPI = PendingIntent.getActivity(
            context, taskId, launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notif = NotificationCompat.Builder(context, App.CHANNEL_ID_TASK)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(listTitle)
            .setContentText("⏰ $timeText")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentPI)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context)
            .notify(taskId, notif)

        //  Jeśli zadanie się powtarza i nie przekroczyliśmy daty końca — zaplanuj następny
        if (isRepeating && intervalName != RepeatInterval.NONE.name) {
            val interval = RepeatInterval.valueOf(intervalName)
            val nextTs = when (interval) {
                RepeatInterval.DAILY   -> timestamp + TimeUnit.DAYS.toMillis(1)
                RepeatInterval.WEEKLY  -> timestamp + TimeUnit.DAYS.toMillis(7)
                RepeatInterval.MONTHLY -> Calendar.getInstance().run {
                    timeInMillis = timestamp
                    add(Calendar.MONTH, 1)
                    timeInMillis
                }
                else -> null
            }

            if (nextTs != null && (repeatEnd < 0L || nextTs <= repeatEnd)) {
                val nextIntent = Intent(context, TaskReminderReceiver::class.java).apply {
                    putExtras(intent.extras!!)
                    putExtra("extra_timestamp", nextTs)
                }
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val pi = PendingIntent.getBroadcast(
                    context, taskId, nextIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        // API 31+: pytamy, czy możemy exact alarms
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                nextTs,
                                pi
                            )
                        } else {
                            alarmManager.set(
                                AlarmManager.RTC_WAKEUP,
                                nextTs,
                                pi
                            )
                        }
                    } else {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            nextTs,
                            pi
                        )
                    }
                } catch (e: SecurityException) {
                    Log.w("TaskReminderReceiver", "Cannot schedule exact alarm, falling back to inexact", e)
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        nextTs,
                        pi
                    )
                }
            }
        }
    }
}
