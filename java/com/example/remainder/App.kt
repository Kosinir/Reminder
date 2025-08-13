package com.example.remainder

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class App : Application(){
    companion object{
        const val CHANNEL_ID_TASK = "tasks_channel"
        const val CHANNEL_NAME_TASKS = "Tasks reminders"
        const val CHANNEL_DESC_TASKS = "Kanał dla przypomień o zadaniach"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                CHANNEL_ID_TASK,
                CHANNEL_NAME_TASKS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC_TASKS
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val mgr = getSystemService(NotificationManager::class.java)
            mgr.createNotificationChannel(channel)
        }
    }
}