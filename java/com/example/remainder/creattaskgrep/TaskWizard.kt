package com.example.remainder.creattaskgrep

import TaskDateStep
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.remainder.viewmodel.TasksViewModel
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.remainder.data.tasks.TaskEntity
import com.example.remainder.data.tasks.RepeatInterval

@Composable
fun TaskWizard(
    rootNavController: NavController,
    tasksViewModel: TasksViewModel
) {
    val wizardNav = rememberNavController()

    var listTitle by rememberSaveable { mutableStateOf("") }
    var tasksList by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var dueTimestamp by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    var isRepeating by rememberSaveable { mutableStateOf(false) }
    var repeatInterval by rememberSaveable { mutableStateOf(RepeatInterval.NONE) }
    var repeatEndTimestamp by rememberSaveable { mutableStateOf(dueTimestamp) }
    var tasksDone by rememberSaveable { mutableStateOf(emptyList<Boolean>()) }

    NavHost(
        navController = wizardNav,
        startDestination = "info"
    ) {
        composable("info") {
            TaskInfoStep(
                listTitle = listTitle,
                onListTitleChange = { listTitle = it },
                tasks = tasksList,
                onTaskChange = { index, value ->
                    tasksList = tasksList.toMutableList().also { it[index] = value }
                },
                onAddTask = {
                    tasksList = tasksList + ""
                    tasksDone = tasksDone + false
                },
                onRemoveTask = { index ->
                    tasksList = tasksList.toMutableList().also { it.removeAt(index) }
                    tasksDone = tasksDone.toMutableList().also { it.removeAt(index) }
                },
                onNext = { wizardNav.navigate("date") },
                onBack = { rootNavController.popBackStack() }
            )
        }
        composable("date") {
            TaskDateStep(
                dueTimestamp = dueTimestamp,
                onDateChange = { dueTimestamp = it },
                onTimeChange = { dueTimestamp = it },
                isRepeating = isRepeating,
                onIsRepeatingChange = { isRepeating = it },
                repeatInterval = repeatInterval,
                onRepeatIntervalChange = { repeatInterval = it },
                repeatEndTimestamp = repeatEndTimestamp,
                onRepeatEndTimestampChange = { repeatEndTimestamp = it },
                onBack = { wizardNav.popBackStack() },
                onNext = {
                    val task = TaskEntity(
                        listTitle = listTitle,
                        tasksList = tasksList,
                        dueTimestamp = dueTimestamp,
                        isRepeating = isRepeating,
                        repeatInterval = if (isRepeating) repeatInterval else RepeatInterval.NONE,
                        repeatEndTimestamp = if (isRepeating) repeatEndTimestamp else null,
                        isDone = false,
                        createdAt = System.currentTimeMillis(),
                        tasksDone = tasksDone
                    )
                    tasksViewModel.addTask(task)
                    rootNavController.popBackStack()
                }
            )
        }
    }
}