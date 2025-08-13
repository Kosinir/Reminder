package com.example.remainder.edittaskgrep

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.remainder.data.tasks.TaskEntity
import com.example.remainder.viewmodel.TasksViewModel

@Composable
fun EditTaskWizard(
    rootNavController: NavHostController,
    tasksViewModel: TasksViewModel,
    taskId: Int
) {
    val entity by tasksViewModel.getTaskById(taskId).collectAsState(initial = null)
    var loadedEntity by remember { mutableStateOf<TaskEntity?>(null) }

    // Trzymamy stan listy i checkboxów
    var tasksList by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var tasksDone by rememberSaveable { mutableStateOf(emptyList<Boolean>()) }

    // Załaduj dane tylko raz
    LaunchedEffect(entity) {
        val task = entity
        if (task != null && loadedEntity == null) {
            loadedEntity = task
            tasksList = task.tasksList
            tasksDone = List(task.tasksList.size) { false }
        }
    }


    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (loadedEntity == null) {
            CircularProgressIndicator()
        } else {
            WizardContent(
                task = loadedEntity!!,
                rootNavController = rootNavController,
                tasksViewModel = tasksViewModel,
                tasksList = tasksList,
                onTasksListChange = { tasksList = it },
                tasksDone = tasksDone,
                onTasksDoneChange = { tasksDone = it }
            )
        }
    }
}

@Composable
private fun WizardContent(
    task: TaskEntity,
    rootNavController: NavHostController,
    tasksViewModel: TasksViewModel,
    tasksList: List<String>,
    onTasksListChange: (List<String>) -> Unit,
    tasksDone: List<Boolean>,
    onTasksDoneChange: (List<Boolean>) -> Unit
) {
    val wizardNav = rememberNavController()
    var listTitle by rememberSaveable(task.id) { mutableStateOf(task.listTitle) }
    var dueTimestamp by rememberSaveable(task.id) { mutableStateOf(task.dueTimestamp) }
    var isRepeating by rememberSaveable(task.id) { mutableStateOf(task.isRepeating) }
    var repeatInterval by rememberSaveable(task.id) { mutableStateOf(task.repeatInterval) }
    var repeatEndTimestamp by rememberSaveable(task.id) { mutableStateOf(task.repeatEndTimestamp) }

    NavHost(
        navController = wizardNav,
        startDestination = "info",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("info") {
            EditTaskInfoStep(
                listTitle = listTitle,
                onListTitleChange = { listTitle = it },
                tasksList = tasksList,
                tasksDone = tasksDone,
                onTaskChange = { idx, v ->
                    val updatedList = tasksList.toMutableList()
                    updatedList[idx] = v
                    onTasksListChange(updatedList)

                    val updatedDone = tasksDone.toMutableList()
                    updatedDone[idx] = false
                    onTasksDoneChange(updatedDone)
                },
                onTaskDoneChange = { idx, v ->
                    val updatedDone = tasksDone.toMutableList()
                    updatedDone[idx] = v
                    onTasksDoneChange(updatedDone)
                },
                onAddTask = {
                    onTasksListChange(tasksList + "")
                    onTasksDoneChange(tasksDone + false)
                },
                onRemoveTask = { idx ->
                    if (idx < tasksList.size) {
                        val updatedList = tasksList.toMutableList()
                        updatedList.removeAt(idx)
                        onTasksListChange(updatedList)
                    }
                    if (idx < tasksDone.size) {
                        val updatedDone = tasksDone.toMutableList()
                        updatedDone.removeAt(idx)
                        onTasksDoneChange(updatedDone)
                    }
                },
                onNext = { wizardNav.navigate("date") },
                onCancel = { rootNavController.popBackStack() }
            )
        }
        composable("date") {
            EditTaskDateStep(
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
                onSave = {
                    val updated = task.copy(
                        listTitle = listTitle,
                        tasksList = tasksList,
                        dueTimestamp = dueTimestamp,
                        isRepeating = isRepeating,
                        repeatInterval = repeatInterval,
                        repeatEndTimestamp = repeatEndTimestamp
                        // Jeśli chcesz dodać tasksDone do bazy, musisz dodać to do TaskEntity
                    )
                    tasksViewModel.updateTask(updated)
                    rootNavController.popBackStack()
                }
            )
        }
    }
}
