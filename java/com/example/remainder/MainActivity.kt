package com.example.remainder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.room.Room
import com.example.remainder.data.db.AppDatabase
import com.example.remainder.data.notes.NotesRepository
import com.example.remainder.data.tasks.TasksRepository
import com.example.remainder.ui.theme.RemainderTheme
import com.example.remainder.viewmodel.NotesViewModel
import com.example.remainder.viewmodel.NotesViewModelFactory
import com.example.remainder.viewmodel.TasksViewModel
import com.example.remainder.viewmodel.TasksViewModelFactory

class MainActivity : ComponentActivity() {
    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "smart-reminder.db"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    private val notesRepository by lazy { NotesRepository(database.noteDao()) }
    private val tasksRepository by lazy { TasksRepository(database.taskDao(), applicationContext) }

    private val notesViewModel: NotesViewModel by viewModels {
        NotesViewModelFactory(notesRepository)
    }
    private val tasksViewModel: TasksViewModel by viewModels {
        TasksViewModelFactory(tasksRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }

            RemainderTheme(darkTheme = isDarkTheme) {
                SmartReminderApp(
                    notesViewModel = notesViewModel,
                    tasksViewModel = tasksViewModel,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }
}